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

    Then in another window
    $ ./gradlew run

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

    Follow instructions to cf push to run the application in pcc.

3. Try different Lucene searches for data in example-region

        gfsh> list lucene indexes

    Note that each server that holds partitioned data for this region has both the ```simpleIndex``` , ```analyzerIndex``` and the ```nestedObjectIndex```. Each Lucene index is stored as a co-located region with the partitioned data region.

     // Search for an exact name match
        gfsh>search lucene --name=simpleIndex --region=example-region --queryStrings="Jive" --defaultField=lastName

     // Search for last name using fuzzy logic: sounds like 'chive'
        gfsh>search lucene --name=simpleIndex --region=example-region --queryStrings="chive~" --defaultField=lastName

     // Do a compound search on first and last name using fuzzy sounds like logic
        gfsh>search lucene --name=simpleIndex --region=example-region --queryStrings="firstName:cat~ OR lastName:chive~" --defaultField=lastName

     // Do a compound search on last name and email using analyzerIndex
        gfsh>search lucene --name=analyzerIndex --region=example-region --queryStrings="lastName:hall~ AND email:Kris.Call@example.com" --defaultField=lastName

     // Do a compound search on nested object with both 5035330001 AND 5036430001 in contacts
     // Note: 5035330001 is the phone number of one of the contacts, 5036430001 is phone number of another contact. Since they are both contacts of this employee, it will lead to this employee.
        gfsh>search lucene --name=nestedObjectIndex --region=/example-region --queryString="5035330001 AND 5036430001" --defaultField=contacts.phoneNumbers

     // If query on 5035330001 AND 5036430002, it will not find the person, because the 2 phone numbers belong to different people's contacts.
        gfsh>search lucene --name=nestedObjectIndex --region=/example-region --queryString="5035330001 AND 5036430002" --defaultField=contacts.phoneNumbers

     // If query on 5035330001 OR 5036430002, it will find 2 people's entries

        gfsh>search lucene --name=nestedObjectIndex --region=/example-region --queryString="5035330001 OR 5036430002" --defaultField=contacts.phoneNumbers

4. Examine the Lucene index statistics

        gfsh>describe lucene index --name=simpleIndex --region=example-region

    Note the statistic show the fields that are indexed and the Lucene analyzer used for each field. In the next example we will specify a different Lucene analyzer for each field. Additional statistics listed are the number of documents (region entries) indexed, number of entries committed as well as the number of queries executed for each Lucene index.

5. Exit gfsh and shut down the cluster

        gfsh>exit
        $ gfsh run --file=scripts/stop.gfsh

6. Clean up any generated directories and files so this example can be rerun.

        $ ./gradlew cleanServer
    

