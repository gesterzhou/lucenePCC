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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvParser {
  private static EnvParser instance;
  private final Pattern p = Pattern.compile("(.*)\\[(\\d*)\\]");

  private EnvParser() {}

  public static EnvParser getInstance() {
    if (instance != null) {
      return instance;
    }
    synchronized (EnvParser.class) {
      if (instance == null) {
        instance = new EnvParser();
      }
    }
    return instance;
  }

  public List<URI> getLocators() throws IOException, URISyntaxException {
    // for non-TLS
    List<URI> locatorList = new ArrayList<URI>();
    Map credentials = getCredentials();

    if (credentials == null) {
      return new ArrayList<URI>(Arrays.asList(new URI("locator://localhost:10334")));
    }
    List<String> locators = (List<String>) credentials.get("locators");
    for (String locator : locators) {
      Matcher m = p.matcher(locator);
      if (!m.matches()) {
        throw new IllegalStateException(
            "Unexpected locator format. expected host[port], got" + locator);
      }
      locatorList.add(new URI("locator://" + m.group(1) + ":" + m.group(2)));
    }
    return locatorList;
  }

  public String getUsername() throws IOException {
    return (String) getUser().get("username");
  }

  public String getPassword() throws IOException {
    return (String) getUser().get("password");
  }

  private Map getUser() throws IOException {
    Map credentials = getCredentials();
    if (credentials == null) {
      return new HashMap();
    }
    List<Map> users = (List) credentials.get("users");
    for (Map map:users) {
      String userName = (String)map.get("username");
      if (userName.startsWith("developer_")) {
        return map;
      }
    }
    return (Map) users.get(0);
  }

  private Map getCredentials() throws IOException {
    Map credentials = null;
    String envContent = (String) System.getenv().get("VCAP_SERVICES");//.asString("{}").get();
    if (envContent == null) {
      return null;
    }

    // new ArrayList();
    ObjectMapper objectMapper = new ObjectMapper();
    Map services = (Map) objectMapper.readValue(envContent, Map.class);
    List gemfireService = getGemFireService(services);
    if (gemfireService != null) {
      Map serviceInstance = (Map) gemfireService.get(0);
      credentials = (Map) serviceInstance.get("credentials");
    }

    return credentials;
  }

  private List getGemFireService(Map services) {
    List l = null;
    for (int i=0; i<20; i++) {
      l = (List)services.get("p-cloudcache");
      if (l == null) {
        try {
          Thread.sleep(5000);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    if (l == null) {
      throw new IllegalStateException("cloud cache service is not bound to this application");
    }
    return l;
  }
}
