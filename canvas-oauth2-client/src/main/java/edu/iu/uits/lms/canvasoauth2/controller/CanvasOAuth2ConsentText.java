package edu.iu.uits.lms.canvasoauth2.controller;

/*-
 * #%L
 * LMS Canvas OAuth2 Client
 * %%
 * Copyright (C) 2015 - 2026 Indiana University
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default English copy for the Canvas OAuth2 consent/connected pages, with an optional per-tool
 * override hook. Most tools in this repo don't wire up a {@code MessageSource}/bundle, so this
 * intentionally uses a plain {@code Map<String, String>} bean instead of Thymeleaf's {@code #{...}}
 * message-expression mechanism - adopting {@code EnableCanvasOAuth2Client} requires no message-bundle
 * setup at all.
 * <p>
 * A tool that wants custom wording defines one optional bean:
 * <pre>{@code
 * @Bean
 * @Qualifier("canvasOAuth2ConsentTextOverrides")
 * public Map<String, String> canvasOAuth2ConsentTextOverrides() {
 *     return Map.of(CanvasOAuth2ConsentText.CONNECT_CANVAS_INSTRUCTIONS, "MyTool needs your permission...");
 * }
 * }</pre>
 * A tool that does nothing gets the defaults below.
 */
@Component
public class CanvasOAuth2ConsentText {

    public static final String CONNECT_CANVAS_HEADING = "connectCanvas.heading";
    public static final String CONNECT_CANVAS_INSTRUCTIONS = "connectCanvas.instructions";
    public static final String CONNECT_CANVAS_CONNECT_BUTTON = "connectCanvas.connectButton";
    public static final String CANVAS_CONNECTED_HEADING = "canvasConnected.heading";
    public static final String CANVAS_CONNECTED_INSTRUCTIONS = "canvasConnected.instructions";
    public static final String CANVAS_CONNECTED_RETURN_BUTTON = "canvasConnected.returnButton";

    private static final Map<String, String> DEFAULTS = Map.of(
            CONNECT_CANVAS_HEADING, "Connect your Canvas account to continue",
            CONNECT_CANVAS_INSTRUCTIONS, "This tool needs your permission to access Canvas on your behalf "
                    + "before it can continue. This only needs to happen once - after you connect, every "
                    + "launch will use your own Canvas permissions instead of asking again.",
            CONNECT_CANVAS_CONNECT_BUTTON, "Connect your Canvas account",
            CANVAS_CONNECTED_HEADING, "You're connected!",
            CANVAS_CONNECTED_INSTRUCTIONS, "Your Canvas account is now connected. Returning you to your "
                    + "course in a moment...",
            CANVAS_CONNECTED_RETURN_BUTTON, "Return to your course now"
    );

    private final Map<String, String> text;

    public CanvasOAuth2ConsentText(
            @Autowired(required = false) @Qualifier("canvasOAuth2ConsentTextOverrides") Map<String, String> overrides) {
        Map<String, String> merged = new HashMap<>(DEFAULTS);
        if (overrides != null) {
            merged.putAll(overrides);
        }
        this.text = Collections.unmodifiableMap(merged);
    }

    public String get(String key) {
        return text.get(key);
    }
}
