package edu.iu.uits.lms.common.server;

/*-
 * #%L
 * lms-canvas-common-configuration
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chmaurer on 7/29/15.
 */
@Builder
public class ServerInfo {
//    canvas-test.iu.edu - esappj125-g.uits.iupui.edu - 2015/7/29 - 8:02 - release/3.0@d317fe5
    @NonNull
    private String serverName;
    private Date buildDate;
    private String gitInfo;
    private String artifactVersion;
    private String environment;

    public static final String BEAN_NAME = "serverInfo";

    @Override
    public String toString() {
        List<String> elements = getElements();
        return StringUtils.join(elements, " - ");
    }

    public List<String> getElements() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/M/dd - H:mm");

        if (buildDate == null) {
            buildDate = new Date();
        }
        String date = dateFormat.format(buildDate);

        String nameAndEnv = serverName;
        if (environment != null) {
            nameAndEnv += "-" + environment;
        }

        List<String> elements = new ArrayList<>();
        elements.add(nameAndEnv);
        elements.add(date);

        if (gitInfo != null) {
            elements.add(gitInfo);
        }

        if (artifactVersion != null) {
            elements.add(artifactVersion);
        }
        return elements;
    }
}
