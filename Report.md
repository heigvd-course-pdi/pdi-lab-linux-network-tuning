Lab report: Linux Network Tuning
================================


1 Baseline measurements
-----------------------

*Document the baseline measurements for streaming and web server scenarios.*

Table of baseline measurements:

| Scenario          | Baseline measurement        |
|-------------------|-----------------------------|
| **Streaming**     |                             |
| **Web server**    |                             |

Direction of the data flow in the streaming test:


2 Measurements under WAN conditions
-----------------------------------

*Document the measurements under WAN conditions for streaming and web server scenarios.*

Table of WAN condition measurements:

| Scenario          | WAN measurements            |
|-------------------|-----------------------------|
| **Streaming**     |                             |
| **Web server**    |                             |


*How much do the results differ from the baseline measurements?*



4 Tuning TCP performance
-------------------------------

### 4.1 Initial Linux kernel parameters

*Document the initial values of the Linux kernel parameters here.*

Table of initial Linux kernel parameters:

| Parameter                              | Initial value                |
|----------------------------------------|------------------------------|
| **net.core.rmem_max**                  |                              |
| **net.core.wmem_max**                  |                              |
| **net.ipv4.tcp_rmem**                  |                              |
| **net.ipv4.tcp_wmem**                  |                              |
| **net.ipv4.tcp_window_scaling**        |                              |


### 4.2 Bandwidth-Delay Product (BDP)

*Document your bandwidth-delay product (BDP) calculation here.*



### 4.3 Hypotheses for tuning

*Document your initial hypotheses for tuning the Linux kernel parameters here. Which kernel parameters do you expect to have the most impact on performance under WAN conditions?*

*This section won't be graded. It is for you to check your understanding of TCP performance.*



### 4.4 Tuning steps and performance impact

*Document your tuning steps and the resulting performance measurements here.*
*Indicate whether you kept or reverted each change.*


| Parameters                          | Old values          | New values           | Stream.  | Web        | Keep |
|-------------------------------------|---------------------|----------------------|----------|------------|------|
| server: ...parameter...             |                     |                      |          |            | y/n  |
| client: ...parameter...             |                     |                      |          |            |      |
|                                     |                     |                      |          |            |      |
|                                     |                     |                      |          |            |      |
|                                     |                     |                      |          |            |      |
|                                     |                     |                      |          |            |      |
|                                     |                     |                      |          |            |      |
|                                     |                     |                      |          |            |      |
|                                     |                     |                      |          |            |      |
|                                     |                     |                      |          |            |      |


### 4.5 Final tuned configuration

*Document the final values of all tuned Linux kernel parameters on the server machine here.*

| Parameters                          | Old values          | New values           |
|-------------------------------------|---------------------|----------------------|
| server: net.ipv4.tcp_wmem  	      |                     |                      |
| client: net.ipv4.tcp_rmem  	      |                     |                      |
| server: net.ipv4.tcp_rmem  	      |                     |                      |
| client: net.ipv4.tcp_wmem  	      |                     |                      |
| server: net.core.wmen_max  	      |                     |                      |
| client: net.core.rmem_max  	      |                     |                      |
| server: net.core.rmen_max  	      |                     |                      |
| client: net.core.wmem_max  	      |                     |                      |
| server: net.ipv4.tcp_window_scaling |                     |                      |
| client: net.ipv4.tcp_window_scaling |                     |                      |

*Compare the resulting performance measurements to the baseline measurements and WAN measurements before tuning.*


| Application       | Before tuning (WAN)      | After tuning               |
|-------------------|--------------------------|----------------------------|
| **Streaming**     |                          |                            |
| **Web server**    |                          |                            |

*Interpret the changes to optimize performance: Did they correspond to your hypotheses? Is there a difference between streaming and web workloads?*



5 Tuning the TCP congestion control algorithm
---------------------------------------------

### 5.1 Default congestion control algorithm on the client and server

*Document the default TCP congestion control algorithm on the client and server machines here.*

| Parameter                           | Client value    | Server value     |
|-------------------------------------|-----------------|------------------|
| **net.ipv4.tcp_congestion_control** |                 |                  |
| **net.core.default_qdisc**          |                 |                  |


### 5.2 Performance of the default congestion control algorithm (cubic) under WAN conditions

*Document the results of the streaming and web server measurements under the new WAN conditions in your report. Use the results from the previous section as a reference.*

| Application       | Previous performance without packet loss | Performance with 0.1% packet loss |
|-------------------|------------------------------------------|-----------------------------------|
| **Streaming**     |                                          |                                   |
| **Web server**    |                                          |                                   |


*How big is the impact of 0.1% packet loss on the performance with the default congestion control algorithm?*



### 5.3 Performance of BBR  under WAN conditions

*Document the results of the streaming and web server measurements with BBR.*

| Application       | Performance with Cubic | Performance with BBR |
|-------------------|------------------------|----------------------|
| **Streaming**     |                        |                      |
| **Web server**    |                        |                      |


*Interpret the changes: Did BBR improve performance compared to cubic?*




6 Analyzing the Web server performance
--------------------------------------

### 6.1 Analyzing the Web traffic

*Add a screenshot of the Wireshark capture showing two consecutive HTTP request-response.*


*Analyze the packet sequence. In particular, determine the amount of data transferred and the duration of each connection (time between the first SYN of the two connections).*
*Then answer the following questions:
* - Why is the Web performance influenced by latency?
* - Why is the Web performance not influenced by packet loss?
* - Why doesn't tuning TCP improve the Web performance measurements?



### 6.2 Analyzing the measurements

*Using Little's Law, estimate the maximum requests per second that can be achieved with the concurrency level used in the `hey` measurements.*


*Compare your estimate to the actual requests per second measured by `hey`.*


*What would happen if you increased the concurrency level in `hey`?*



### 6.3 Measuring the Web server performance limit

*Document your CPU usage measurements and the estimated maximum requests per second.*



### 6.4 Web server measurements with increasing concurrency levels

*Document the results of the Web server measurements with increasing concurrency levels in your report.*

| Concurrent connections | Requests per second | Average response time |
|------------------------|---------------------|-----------------------|
|  100                   |                     |                       |
|  200                   |                     |                       |
|  300                   |                     |                       |
|  400                   |                     |                       |
|  500                   |                     |                       |
|  600                   |                     |                       |

*Interpret the results: How do the requests per second and average response time change with increasing concurrency?*



7 Conclusions
-------------

*Summarize your findings and results in your report.*


