/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.policy.password;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

public class Utils {

    public static final String IDM_PROPERTIES_FILE = "identity-mgt.properties";
    public static final String PASSWORD_EXP_IN_DAYS = "Authentication.Policy.Password.Reset.Time.In.Days";
    public static final String LAST_PASSWORD_CHANGED_TIMESTAMP_CLAIM = "http://wso2.org/claims/lastPasswordChangedTimestamp";
    public static final int DEFAULT_PASSWORD_EXP_IN_DAYS = 30;

    private static Properties properties = new Properties();

    private static final Log log = LogFactory.getLog(Utils.class);

    static {
        loadProperties();
    }

    private Utils() {
    }

    /**
     * loading the identity-mgt.properties file.
     */
    public static void loadProperties() {

        FileInputStream fileInputStream = null;
        String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "identity" + File.separator;

        try {
            configPath = configPath + IDM_PROPERTIES_FILE;
            fileInputStream = new FileInputStream(new File(configPath));
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("identity-mgt.propertie file not found in " + configPath, e);
        } catch (IOException e) {
            throw new RuntimeException("identity-mgt.propertie file reading error from " + configPath, e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception e) {
                    log.error("Error occured while closing stream :" + e);
                }
            }
        }
    }

    /**
     * 
     * @return
     */
    public static int getPasswordExpirationInDays() {
        if (properties.get(PASSWORD_EXP_IN_DAYS) != null) {
            return Integer.parseInt((String) properties.get(PASSWORD_EXP_IN_DAYS));
        } else {
            return DEFAULT_PASSWORD_EXP_IN_DAYS;
        }
    }

}