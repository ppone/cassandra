package org.apache.cassandra.cql;
/*
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 */


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.SchemaLoader;
import org.apache.cassandra.config.*;
import org.apache.cassandra.service.AbstractCassandraDaemon;
import org.apache.cassandra.service.EmbeddedCassandraService;
import org.junit.BeforeClass;

/**
 * The abstract BaseClass.
 */
public abstract class EmbeddedServiceBase
{
    /** The embedded server cassandra. */
    private static EmbeddedCassandraService cassandra;

    static
    {
        AbstractCassandraDaemon.initLog4j();
    }

    @BeforeClass
    public static void cleanUpOldStuff() throws IOException
    {
        CleanupHelper.cleanupAndLeaveDirs();
    }
    
    /**
     * Start cassandra server.
     * @throws ConfigurationException 
     *
     * @throws Exception the exception
     */
    public static void startCassandraServer() throws IOException, ConfigurationException
    {
        if (!checkIfServerRunning())
        {
            loadData();
            cassandra = new EmbeddedCassandraService();
            cassandra.start();
        }
    }

    
    /**
     * Load yaml tables.
     *
     * @throws ConfigurationException the configuration exception
     */
    static void loadData() throws ConfigurationException
    {
        for (KSMetaData table : SchemaLoader.schemaDefinition())
        {
            for (CFMetaData cfm : table.cfMetaData().values())
            {
                Schema.instance.load(cfm);
            }
            Schema.instance.setTableDefinition(table, Schema.instance.getVersion());
        }
    }
    /**
     * Check if server running.
     *
     * @return true, if successful
     */
    static boolean checkIfServerRunning()
    {
        try
        {
            Socket socket = new Socket("127.0.0.1", 9170);
            return socket.getInetAddress() != null;
        } 
        catch (UnknownHostException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
    }
}
