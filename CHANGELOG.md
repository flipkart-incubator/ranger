## 1.0 SNAPSHOT

* The nodeData T in the ServiceNode has been broken from the Criteria used in the shard selectors, to help for dynamic binding of criteria, so node filters can be dictated by the clients who are using the bundle, basis any of the client specific parameters.
* A service hub has been introduced to help with not having to do the boilerplate code everytime a finder is initialized. Particularly handy when there are tens of services to be initialized
* Signals have been introduced as a first class concept to help with the lifecycle management of serviceprovider and servicefinder 
* The ranger module has been further broken down into ranger-core (To define the core framework of ranger), ranger-zk and ranger-http (To appropriate the ranger-core with the respective implementations for zk and http data sources). ranger-zk-client and ranger-http-client(To appropriate the finderhub implementations for zk and http data sources respectively)
* A sample ranger-server has been added to facilitate writing a http finder backend, atop a service huub (In this case the zk service hub)
* An extensive test suite to test the multiple modules has been added as well. 