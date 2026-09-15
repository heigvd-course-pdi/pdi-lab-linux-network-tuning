Linux Network Tuning Lab
========================

In this lab, your task is to improve the network performance of Linux machines (server and client) by tuning various Linux kernel parameters. You will use benchmarking tools to measure the performance before and after applying the tuning parameters.

The learning goals of this lab are:

By the end of this lab, you will be able to:

- Measure baseline network performance using standard tools
- Identify network performance bottlenecks
- Apply kernel-level TCP/IP tuning parameters
- Compare different TCP congestion control algorithms
- Understand the relationship between buffer sizes and Bandwidth-Delay Product (BDP)
- Understand the problems of measuring Web server performance

> [!NOTE]
> Document your findings and results in a report. Use the file `Report.md` as a template. You can write it in English or French.


Problem description
-------------------

A company is experiencing network performance issues on their Linux-based servers. Your task is to analyze the current network performance, identify bottlenecks, and apply various Linux kernel tuning parameters to improve the performance.

Lab setup
---------

You will need two Linux machines (server and client) connected over a network. You can use physical machines or virtual machines for this lab.

- Hardware: 2 CPU cores, 2 GB RAM.
- OS: a recent Linux distribution such as Ubuntu 22.04 or later
- Network: at least 100 Mbps connection between the two machines
- You need to have root access to both machines.

### Software requirements

```bash
# Install on both machines
sudo apt update
sudo apt install -y iperf3 ethtool net-tools iproute2 hey
sudo apt install -y iftop libbpf-tools sysstat # Monitoring tools
sudo apt install -y docker-compose-plugin  # For Docker Compose v2
```

Also verify kernel version: Linux 4.9 or higher is needed:

``` 
uname -r
```


Baseline measurements
---------------------

We will consider two different load scenarios:

- **Streaming**: optimize the throughput for large files (e.g., video streaming or file downloads).
- **Web**: optimize requests per second for a web server under load.

In the following, we will use `$SERVER_IP` and `$CLIENT_IP` to refer to the IP addresses of the server and client machines, respectively. Use the actual IP addresses of your two virtual machines.


### Streaming baseline measurement

In a first step, we will measure the maximum achievable throughput between the server and client machines under ideal conditions.

`iperf3` is a standard Linux tool for measuring network performance. We can use it to measure the throughput between the server and client machines.

Perform the following steps to measure the baseline streaming performance:

1. On the server machine, start the `iperf3` server:

```bash
iperf3 -s
```

We will use the standard tool `iperf3` to measure the network performance between the server and client machines.

On the server, start the iperf3 server:

```bash
iperf3 -s
```

On the client, run the iperf3 client to generate 4 parallel streams for 60 seconds:

```bash
iperf3 -c $SERVER_IP -t 30 -P 4 -i 5 --reverse
```

Check the `iperf3` documentation to understand the options used.

If `iperf3` cannot connect to the server, check firewall settings on both machines.

> [!IMPORTANT]
> **Exercise 1.1**
> 
> Document the throughput result in your report.
> During the test, monitor the flows using `tcptop`. In which direction does the data flow? From client to server or server to client?


### Web baseline measurement

A simple Web server implemented in Java is provided in the `java-server` directory. Start the server on the server machine using Docker Compose:

```bash
cd java-server
docker compose up --build
```

The Web server responds to HTTP GET requests on the URL: `http://$SERVER_IP:8080`. Internally, the Web server connects to a database, in order to simulate a realistic workload. It uses Javalin and Java virtual threads to handle multiple requests concurrently.

Several tools exist to benchmark Web servers.

| Tool                 | Description                                      |
|----------------------|--------------------------------------------------|
| **ab** (ApacheBench) | Simple command-line tool, but now outdated       |
| **hey**              | Similar to ab, but modern, implemented in Go     |
| **wrk**              | Modern HTTP benchmarking tool with Lua scripting |
| **Locust**           | Can simulate complex user behaviors, with Web UI |
| **JMeter**           | Powerful GUI tool for load testing               |

In this lab, we will use `hey`, which has already been installed with `apt` at the beginning of the lab.

As baseline measurement, we will measure the maximum number of requests per second.

Use Hey to generate requests during 30 seconds with 100 concurrent connections.
The option `--disable-keepalive` ensures that each request opens a new TCP connection, much like a real web browser would do.

```bash
hey -disable-keepalive -z 30s -c 100 http://$SERVER_IP:8080/
```


**You will need to run the test several times and take the best result.**

> [!IMPORTANT]
> **Exercise 1.2**
> 
> Document the requests per second result in your report.


Simulating WAN conditions
-------------------------

After having measured the performance under ideal conditions, we will now simulate more realistic network conditions on a WAN using `tc`.
The `tc` command allows us to add network impairments such as latency, packet loss, and bandwidth limitations.

Run the following command **on the server machine**.  It simulated a network with 100 ms latency for the outgoing traffic on the server:

```bash
# Simulate WAN conditions.
# Run this on the server machine
sudo tc qdisc add dev ens3 root netem delay 100ms

# Verify the settings:
sudo tc qdisc show dev ens3
```

If the command fails with an error such as "Exclusivity flag on, cannot modify", try deleting any existing qdisc first, then reapply the command above:

```bash
sudo tc qdisc del dev ens3 root
```


Measurements under WAN conditions
---------------------------------

Repeat the streaming and web server measurements under the simulated WAN conditions. The results should be significantly lower than under ideal conditions.

You should perform the tests several times and take the best result.

> [!IMPORTANT]
> **Exercise 2**
>
> Document the results of the streaming and web server measurements under WAN conditions in your report.
> How do the results compare to the baseline measurements?


Tuning TCP performance
----------------------

Now you can start improving the performance by tuning the Linux kernel parameters for TCP. Since TCP is bidirectional, you will need to tune parameters on both the server and client machines.

### Initial Linux kernel parameters

The Linux network stack has many parameters that can be tuned to improve performance. The list below shows the parameters you will investigate in this lab:

| Parameter                              | Description                                             |
|----------------------------------------|---------------------------------------------------------|
| **net.core.rmem_max**                  | Maximum receive socket buffer size                      |
| **net.core.wmem_max**                  | Maximum send socket buffer size                         |
| **net.ipv4.tcp_rmem**                  | TCP receive buffer size settings                        |
| **net.ipv4.tcp_wmem**                  | TCP send buffer size settings                           |
| **net.ipv4.tcp_window_scaling**        | TCP window scaling                                      |

Many more network-related parameters exist in the Linux kernel. The [Sysctl documentation](https://www.kernel.org/doc/html/latest/networking/ip-sysctl.html) provides a detailed documentation.

You can check and change the values of these parameters using the `sysctl` command, for example:

```bash
# Display the current value
sysctl net.core.rmem_max
# Change the value (temporary, until next reboot)
sysctl -w net.core.rmem_max=16777216
```

If you want to make the changes permanent, you can create a configuration file under `/etc/sysctl.d/` with the desired settings. For example:

```bash
# File /etc/sysctl.d/99-network-tuning.conf
net.core.rmem_max=16777216
...
```

The settings will be applied at the next reboot or you can apply them immediately using `sudo sysctl -p /etc/sysctl.d/99-network-tuning.conf`.

> [!IMPORTANT]
> **Exercise 4.1**
>
> Obtain the current values of the above parameters on both server and client machines. Document the values in a table in your report.


### Planning your tuning

Use your knowledge of TCP to guide your tuning efforts. In particular, consider the following aspects:

- **Bandwidth-Delay Product (BDP)**: What is the bandwidth-delay product based on the maximum throughput under ideal conditions and the simulated round-trip time (RTT)?
- **Buffer sizes**: Which buffer sizes need to be adjusted to accommodate the BDP? Do you need to increase send or receive buffers?
- **Window scaling**: Should window scaling be enabled?

> [!IMPORTANT]
> **Exercise 4.2**
>
> Document your bandwidth-delay product (BDP) calculation in your report.

> [!IMPORTANT]
> **Exercise 4.3**
>
> Document your hypotheses for tuning the Linux kernel parameters in your report.


### Tuning steps

Now change the Linux kernel parameters step by step, both on the server and the client.

- Change one of the parameters to the new value. You may change different parameters on server and client, e.g., send and receive buffer sizes.
- Measure the streaming and web server performance (best of several runs) under WAN conditions.
- Document the results in your report.
- If the performance improves, keep the new value. Otherwise, revert to the previous value.


> [!IMPORTANT]
> **Exercise 4.4**
>
> For each change, document the resulting performance measurements in your report.
> Document whether you kept or reverted the change.


### Final tuned configuration

> [!IMPORTANT]
> **Exercise 4.5**
> 
> Document the final values of all tuned Linux kernel parameters on the server machine in your report.
> Compare the resulting performance measurements to the baseline measurements and WAN measurements before tuning.
> Interpret the changes to optimize performance: Did they correspond to your hypotheses? Is there a difference between streaming and web workloads?



Tuning the TCP congestion control algorithm
-------------------------------------------

TCP on Linux supports multiple congestion control algorithms. The default algorithm (called 'cubic') may not be optimal for high-latency or high-bandwidth networks.

Congestion control is especially important on WANs with high latency and potential packet loss.

Check the current congestion control algorithm on both server and client machines:

```bash
sysctl net.ipv4.tcp_congestion_control
sysctl net.core.default_qdisc
```

> [!IMPORTANT]
> **Exercise 5.1**
>
> Document the current congestion control algorithm on both server and client machines in your report.

### Simulating a WAN network with packet loss

In order to evaluate the performance of different congestion control algorithms, we will simulate a WAN network with both latency and packet loss. Run the following command on the server machine to add 100 ms latency and 0.1% packet loss:

```bash
# Remove the previous qdisc configuration
sudo tc qdisc del dev ens3 root

# Simulate WAN conditions with latency and packet loss
sudo tc qdisc add dev ens3 root netem delay 100ms loss 0.1%

# Verify the settings:
sudo tc qdisc show dev ens3
```

### Performance of the default congestion control algorithm (cubic) under WAN conditions

Repeat the streaming and web server measurements under the new WAN conditions with packet loss.

> [!IMPORTANT]
> **Exercise 5.2**
>
> Document the results of the streaming and web server measurements under the new WAN conditions in your report
> How big is the impact of 0.1% packet loss on the performance with the default congestion control algorithm?

### Tuning the congestion control algorithm to BBR

BBR (Bottleneck Bandwidth and Round-trip propagation time) is Google's modern congestion control algorithm designed for better performance on modern networks.

Change the congestion control algorithm to BBR on the **server**:

```bash
# Change congestion control to BBR on the server
# Check if BBR is available
sysctl net.ipv4.tcp_available_congestion_control

# If BBR is not listed, load the kernel module
sudo modprobe tcp_bbr

# Set BBR as the default
sudo sysctl -w net.ipv4.tcp_congestion_control=bbr

# Also need to set the qdisc to fq (fair queue)
sudo sysctl -w net.core.default_qdisc=fq

# Verify
sysctl net.ipv4.tcp_congestion_control
```

### Performance of BBR  under WAN conditions

Repeat the streaming and web server measurements under the new WAN conditions but with BBR congestion control on the server.

> [!IMPORTANT]
> **Exercise 5.3**
>
> Document the results of the streaming and web server measurements with BBR.
> Interpret the changes: Did BBR improve performance compared to cubic?



Analyzing the Web server performance
------------------------------------

We've seen that it is possible to significantly improve streaming performance by tuning TCP parameters its congestion control algorithm. This is good news!

We've also seen that the Web server performance strongly depends on the network latency. The packet loss of 0.1%, however, had little impact on the Web performance, in contrast to streaming.

And we've seen that tuning TCP had little effect on the measurements for the Web server.
Why didn't tuning TCP improve our measurements for the Web server?

Let's analyze the Web application behavior in more detail.

### Analyzing the Web traffic

To understand the Web server performance measurements, we need to analyze the actual traffic between the Web client and server.

Capture the traffic between the Web client and server for a couple of requests. You can use Wireshark from any machine that can reach the Web server. Or you can use a command such as `sudo tcpdump -i ens3 -w webclient-100ms.pcap port 8080` on the client machine to capture the traffic, then transfer the pcap file to your local machine with `scp`, and open it in Wireshark.

> [!IMPORTANT]
> **Exercise 6.1**
>
> Add a screenshot of the Wireshark capture showing two consecutive HTTP request-response in your report.
> Analyze the packet sequence. In particular, determine the amount of data transferred and the duration of each connection (time between the first SYN of the two connections).
> Then answer the following questions:
> - Why is the Web performance influenced by latency?
> - Why is the Web performance not influenced by packet loss?
> - Why doesn't tuning TCP improve the Web performance measurements?

### Analyzing the measurements

How can we explain the Web performance results from `hey`?

You've determined the connection duration from the Wireshark capture. You also know the number of concurrent connections used by `hey`. Using Little's Law (L = λW), you can estimate the maximum requests per second λ that can be achieved with the given concurrency level.

> [!IMPORTANT]
> **Exercise 6.2**
>
> Using Little's Law, estimate the maximum requests per second that can be achieved with the concurrency level used in the `hey` measurements.
> Compare your estimate to the actual requests per second measured by `hey`.
> What would happen if you increased the concurrency level in `hey`?

I our case, we should assume an unlimited number of users accessing the Web server concurrently. Therefore, we should increase the concurrency level until we reach the maximum requests per second.

Measuring the Web server performance limit
------------------------------------------

The previous analysis shows that the current test does not measure the maximum capacity of the Web server. Had we used a higher concurrency level, `hey` would probably have been able to achieve a higher requests per second rate.

This is an important point to understand: measuring Web server performance is more difficult than measuring streaming performance. We must be careful to choose an appropriate traffic model that reflects the real-world usage of the Web server.

Based on the previous analysis, we're going to repeat the Web server measurements, but now with increasing numbers of concurrent connections.

We start by estimating the performance limit of the server.

- On the server machine, monitor the CPU usage using `htop`. The performance is very likely limited by CPU, not by RAM or network.
- On the client machine, run `hey` with 100 concurrent connections for 30 seconds. Note the approximative average CPU usage on the server during the test.
- Estimate the maximum number of requests per second that the server can handle before reaching 100% CPU usage (this uses the operational 'bottleneck law').

> [!IMPORTANT]
> **Exercise 6.3**
>
> Document your CPU usage measurements and the estimated maximum requests per second in your report.

Then, repeat the Web server measurements with increasing concurrency levels, e.g., 100, 200, 300, ..., until the performance results no longer increase or start to decrease. 
Try monitoring the CPU usage on the server during the tests to confirm that the server is fully loaded.
During the tests, watch out for errors reported by `hey` at tbe end of the output, which indicate that the server is overloaded.

> [!IMPORTANT]
> **Exercise 6.4**
>
> Document the results of the Web server measurements with increasing concurrency levels in your report (requests per second and average response time).
> Interpret the results: How do the requests per second and average response time change with increasing concurrency?

An important lesson from this section is: to measure the maximum performance of a Web server, you must ramp up the traffic (concurrent connections) until the server is saturated. You should measure the ressource usage (CPU, RAM, network) on the server to confirm that it is fully loaded.


Cleaning up
-----------

To remove the artifical impairments created to simulate WAN conditions, run the following command on the server machine:

```bash
sudo tc qdisc del dev ens3 root
```

On the server machine, stop the Docker Compose Web server:

```bash
cd java-server
docker compose down
```

Conclusions
-----------

> [!IMPORTANT]
> **Exercise 7**
>
> What did you learn from this lab? Summarize your findings and results in your report.
