# Distributed System Experiment

This is a project built for learning the concepts in distributed system.

All parts built and tested by following instructions on [here](https://gortonator.github.io/bsds-6650/).

## part 1

This part is mainly for building a local client and two servers (a Java one and a Go one) and comparing their multithreaded performance. The data temporarily stored in memory. In actual load testing sessions, both servers were deployed and ran on AWS EC2.

The setup scenario is a simplified music service and the API used is [here](https://app.swaggerhub.com/apis/IGORTON/AlbumStore/1.0.0) which provides functionality about album storing and retrieving album by key.

The test was using different sizes of thread groups and each thread ran 2000 requests concurrently. The test result was not perfect, but it was on the right track: the throughput per second for consuming 600,000 requests can be over 2000 and Go did perform faster tha Java server in general. One example of the test window is like:
```
<pic1>
```
And the throughput change over a test session is like:
```
<pic2>
```

## part 2

This part builds a database on top of the Java version of part 1 and compares the performance before and after adding a AWS load balancer into the system.

I chose Redis as database because I wanted to try it. 

After adding a load balancer, the design looks like this:
```
<pic3>
```
Same test loads were used on testing the single server vs two servers plus an application load balancer. Test results clearly showed increase in throughput performance by 50% after adding the load balancer:
```
<pic4>
```
Since the best throughput didn't exceed 2000, I later tried to change EC2s' types from t2.micro to t2.medium and it finally went over 2000.

## part 3

This part builds an asynchronous request processing on top of the single server version of part 2. The data for such processing is a new use case - like and dislikes from users for albums. The new endpoint is shown [here](https://app.swaggerhub.com/apis/IGORTON/AlbumStore/1.1#).

Using RabbitMQ broker, I modified the server code and implemented a Consumer:
```
<pic5>
```
While loading testing the system, RabbitMQ performed as expected. This is one moment of the broker performance, production rate is close to consumption rate:
```
<pic6>
```
And this is a 10-min look:
```
<pic7>
```

## part 4
 
This part builds a new [endpoint](https://app.swaggerhub.com/apis/IGORTON/AlbumStore/1.2#) for querying the number of likes and dislikes for an album.

The test then is to query the number of likes and dislikes in multithreaded environment and try to get the best response time as the system can.

