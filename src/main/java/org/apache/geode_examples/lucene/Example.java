/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode_examples.lucene;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.lucene.LuceneQuery;
import org.apache.geode.cache.lucene.LuceneQueryException;
import org.apache.geode.cache.lucene.LuceneService;
import org.apache.geode.cache.lucene.LuceneServiceProvider;
import org.apache.geode.internal.cache.LocalRegion;

public class Example {
  // These index names are predefined in gfsh scripts
  final static String SIMPLE_INDEX = "simpleIndex";
  final static String ANALYZER_INDEX = "analyzerIndex";
  final static String NESTEDOBJECT_INDEX = "nestedObjectIndex";

  // These region names are prefined in gfsh scripts
  final static String EXAMPLE_REGION = "example-region";

  public static void main(String[] args) throws LuceneQueryException, InterruptedException {
    // connect to the locator using default port 10334
    Properties props = new Properties();
    props.setProperty("security-client-auth-init",
        "org.apache.geode_examples.lucene.ClientAuthInitialize.create");
    ClientCacheFactory ccf = new ClientCacheFactory(props);

    try {
      List<URI> locatorList = EnvParser.getInstance().getLocators();
      for (URI locator : locatorList) {
        ccf.addPoolLocator(locator.getHost(), locator.getPort());
      }

      ClientCache cache = ccf.set("log-level", "debug").create();

      // create a local region that matches the server region
      Region<Integer, EmployeeData> region =
          cache.<Integer, EmployeeData>createClientRegionFactory(ClientRegionShortcut.CACHING_PROXY)
              .create("example-region");
      if (args.length >= 1) {
        region.put(100871, new EmployeeData(args[0], args[0], 1, "a.b@pivotal.io",
                100, 40, new ArrayList()));
      }
      insertValues(region);
      query(cache);
      queryNestedObject(cache);
      LuceneService service = LuceneServiceProvider.get(cache);
      service.waitUntilFlushed("simpleIndex", "example-region", 30, TimeUnit.SECONDS);
      service.waitUntilFlushed("analyzerIndex", "example-region", 30, TimeUnit.SECONDS);
      service.waitUntilFlushed("nestedObjectIndex", "example-region", 30, TimeUnit.SECONDS);
      cache.close();
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException("Could not deploy Application", e);
    }
  }

  private static void query(ClientCache cache) throws LuceneQueryException {
    LuceneService lucene = LuceneServiceProvider.get(cache);
    LuceneQuery<Integer, EmployeeData> query = lucene.createLuceneQueryFactory()
        .create(SIMPLE_INDEX, EXAMPLE_REGION, "firstName:Chris~2", "firstname");
    System.out.println("Employees with first names like Chris: " + query.findValues());
  }

  private static void queryNestedObject(ClientCache cache) throws LuceneQueryException {
    LuceneService lucene = LuceneServiceProvider.get(cache);
    LuceneQuery<Integer, EmployeeData> query = lucene.createLuceneQueryFactory().create(
        NESTEDOBJECT_INDEX, EXAMPLE_REGION, "5035330001 AND 5036430001", "contacts.phoneNumbers");
    System.out.println("Employees with phone number 5035330001 and 5036430001 in their contacts: "
        + query.findValues());
  }

  public static void insertValues(Map<Integer, EmployeeData> region) {
    // insert values into the region
    String[] firstNames = "Alex,Bertie,Kris,Dale,Frankie,Jamie,Morgan,Pat,Ricky,Taylor".split(",");
    String[] lastNames = "Able,Bell,Call,Driver,Forth,Jive,Minnow,Puts,Reliable,Tack".split(",");
    String[] contactNames = "Jack,John,Tom,William,Nick,Jason,Daniel,Sue,Mary,Mark".split(",");
    int salaries[] = new int[] {60000, 80000, 75000, 90000, 100000};
    int hours[] = new int[] {40, 40, 40, 30, 20};
    int emplNumber = 10000;
    for (int index = 0; index < firstNames.length; index++) {
      emplNumber = emplNumber + index;
      Integer key = emplNumber;
      String email = firstNames[index] + "." + lastNames[index] + "@example.com";
      // Generating random number between 0 and 100000 for salary
      int salary = salaries[index % 5];
      int hoursPerWeek = hours[index % 5];

      ArrayList<Contact> contacts = new ArrayList();
      Contact contact1 = new Contact(contactNames[index] + " Jr",
          new String[] {"50353" + (30000 + index), "50363" + (30000 + index)});
      Contact contact2 = new Contact(contactNames[index],
          new String[] {"50354" + (30000 + index), "50364" + (30000 + index)});
      contacts.add(contact1);
      contacts.add(contact2);
      EmployeeData val = new EmployeeData(firstNames[index], lastNames[index], emplNumber, email,
          salary, hoursPerWeek, contacts);
      region.put(key, val);
    }
  }
}
