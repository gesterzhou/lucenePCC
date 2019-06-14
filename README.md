<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# lucenePCC
Example to run gemfire lucene application on both local machine and Pivotal Cloud Cache. 

This example demonstrates the use of a simple Lucene index. Lucene provides
a powerful text search and analysis.

In this example, two servers host a single partitioned region with entries
that represent employee information. The example indexes the first and last
names of employees.

This example assumes that Java and Gemfire 9.7.1 are installed. Make sure gfsh is in 
path.

1. Build the example (with `EmployeeData` class)

    $ ./gradlew build

2. Run a script that starts a locator and two servers, creates a Lucene index
called ```simpleIndex```, and then creates the ```example-region``` region.
A Lucene index must be created before creating the region.

# If run in local machine:
    $ gfsh run --file=scripts/start.gfsh

# If run in PCC:
    $ cf login --skip-ssl-validation -a "https://api.sys.${TS_G_ENV}.cf-app.com"
    when prompted user name and password, use 'admin' and PAS's UAA admin's password.
    
    $ cf create-space my-space
    $ cf target -s "my-space"
    $ cf create-service p-cloudcache dev-plan my-cloudcache
    $ cf create-service-key my-cloudcache my-service-key
    $ cf service-key my-cloudcache my-service-key
    Getting key my-service-key for service instance my-cloudcache as admin...

    {
     "distributed_system_id": "0",
     "locators": [
      "10.0.8.5[55221]"
     ],
     "urls": {
      "gfsh": "https://cloudcache-16786641-6250-4ff4-97f1-18798af1ccdc.sys.rome.cf-app.com/gemfire/v1",
      "pulse": "https://cloudcache-16786641-6250-4ff4-97f1-18798af1ccdc.sys.rome.cf-app.com/pulse"
     },
     "users": [
      {
       "password": "Ada3wkSjZsYC4oCIhywhA",
       "roles": [
        "cluster_operator"
       ],
       "username": "cluster_operator_XovqBdQ0jSHpe9jcYYaA"
      },
      {
       "password": "QVcSaw9pZOKIkoI8UyR8w",
       "roles": [
        "developer"
       ],
       "username": "developer_jNnlmXMEdwsrmaDayfNKg"
      }
     ],
     "wan": {
      "sender_credentials": {
       "active": {
        "password": "yG5Ch0wZ0we8nuGpDeLw",
        "username": "gateway_sender_zGNjzX6UGU8XhYisi3Pw"
       }
      }
     }
    }

    $ gfsh connect --use-http=false --use-ssl --skip-ssl-validation=true --url="https://cloudcache-16786641-6250-4ff4-97f1-18798af1ccdc.sys.rome.cf-app.com/gemfire/v1"
    gfsh> run --file=scripts/start_pcc.gfsh
    
