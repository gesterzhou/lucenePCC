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

import org.apache.geode.LogWriter;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;

import java.io.IOException;
import java.util.Properties;

public class ClientAuthInitialize implements AuthInitialize {
  private EnvParser env = EnvParser.getInstance();

  public static final String USER_NAME = "security-username";
  public static final String PASSWORD = "security-password";

  public static AuthInitialize create() {
    return new ClientAuthInitialize();
  }

  @Override
  public void close() {}

  @Override
  public Properties getCredentials(Properties arg0, DistributedMember arg1, boolean arg2)
      throws AuthenticationFailedException {
    Properties props = new Properties();
    try {
      String username = env.getUsername() == null ? "" : env.getUsername();
      String password = env.getPassword() == null ? "" : env.getPassword();
      props.put(USER_NAME, username);
      props.put(PASSWORD, password);
    } catch (IOException e) {
      throw new AuthenticationFailedException(
          "Exception reading username/password from env variables ", e);
    }
    return props;
  }

  @Override
  public void init(LogWriter arg0, LogWriter arg1) throws AuthenticationFailedException {}
}
