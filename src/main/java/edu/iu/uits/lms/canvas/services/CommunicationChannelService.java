package edu.iu.uits.lms.canvas.services;

/*-
 * #%L
 * LMS Canvas Services
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

import edu.iu.uits.lms.canvas.model.CommunicationChannel;
import edu.iu.uits.lms.canvas.model.CommunicationChannelCreateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to get various communication channel related things
 */
@Service
@Slf4j
public class CommunicationChannelService extends SpringBaseService {
    private static final String CC_BASE_URI = "{url}/users/{user_id}/communication_channels";
    private static final UriTemplate CC_BASE_TEMPLATE = new UriTemplate(CC_BASE_URI);

    private static final String CC_URI = CC_BASE_URI + "/{id}";
    private static final UriTemplate CC_TEMPLATE = new UriTemplate(CC_URI);

    /**
     * Get all communication channels of a given type for a given user id
     * @param canvasUserId The canvas user id
     * @param type Type of channels to return
     * @return List of CommunicationChannels
     */
    public List<CommunicationChannel> getCommunicationChannels(String canvasUserId, String type) throws CanvasUserNotFoundException {
        List<CommunicationChannel> typedChannels = new ArrayList<>();
        List<CommunicationChannel> allChannels = getCommunicationChannels(canvasUserId);
        for (CommunicationChannel communicationChannel : allChannels) {
            if (type.equals(communicationChannel.getType())) {
                typedChannels.add(communicationChannel);
            }
        }
        return typedChannels;
    }

    /**
     * Get all communication channels for a given user id
     * @param canvasUserId The canvas user id
     * @return List of CommunicationChannels
     */
    public List<CommunicationChannel> getCommunicationChannels(String canvasUserId) throws CanvasUserNotFoundException {
        URI uri = CC_BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasUserId);
        log.debug("{}", uri);

        return doGet(uri, CommunicationChannel[].class);
    }

    /**
     * Get the communication channel that contains the primary email address
     * @param canvasUserId The canvas user id
     * @return The CommunicationChannel
     */
    public CommunicationChannel getPrimaryEmailChannel(String canvasUserId) throws CanvasUserNotFoundException {
        List <CommunicationChannel> communicationChannels = getCommunicationChannels(canvasUserId);
        CommunicationChannel communicationChannel = null;
        // Start with a reasonably high position so that anything legitimate returned from the service will always be lower
        int currentChannelPosition = 999;
        if (communicationChannels != null) {
            for (CommunicationChannel channel : communicationChannels) {
                // Iterate through all channels, looking only for the ones of type 'email'.
                // Then, the one we want is the channel with the lowest position
                if (CommunicationChannel.EMAIL_TYPE.equals(channel.getType()) && channel.getPosition() < currentChannelPosition) {
                    communicationChannel = channel;
                    currentChannelPosition = channel.getPosition();
                }
            }
        }

        return communicationChannel;
    }

    /**
     * Get the communication channel that contains the given email address
     * @param canvasUserId The canvas user id
     * @param email Email address to lookup
     * @return The CommunicationChannel
     */
    public CommunicationChannel getChannelByEmail(String canvasUserId, String email) throws CanvasUserNotFoundException {
        List <CommunicationChannel> communicationChannels = getCommunicationChannels(canvasUserId);
        CommunicationChannel communicationChannel = null;
        if (communicationChannels != null) {
            communicationChannel = getChannelByEmail(email, communicationChannels);
        }

        return communicationChannel;
    }

    /**
     * Get the communication channel that contains the given email address
     * @param email Email address to lookup
     * @param channels List of channels to search for the given email
     * @return The CommunicationChannel
     */
    public CommunicationChannel getChannelByEmail(String email, List<CommunicationChannel> channels) {
        CommunicationChannel communicationChannel = null;
        for (CommunicationChannel channel : channels) {
            // Iterate through all channels, looking only for the ones of type 'email'.
            // Then, the one we want is the channel where the email matches
            if (CommunicationChannel.EMAIL_TYPE.equals(channel.getType()) && channel.getAddress().equals(email)) {
                communicationChannel = channel;
                break;
            }
        }
        return communicationChannel;
    }

    /**
     * Delete a specific communication channel for a given user
     * @param channelId Channel id to delete
     * @param canvasUserId The canvas user id containing the desired channel
     */
    public void deleteCommunicationChannel(String channelId, String canvasUserId) {
        URI uri = CC_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasUserId, channelId);
        log.debug("{}", uri);

        try {
            this.restTemplate.delete(uri);
        } catch (HttpClientErrorException hcee) {
            throw new RuntimeException("Failed to delete communication_channel '" + channelId + "' for user '" + canvasUserId + "'", hcee);
        }
    }

    /**
     * Create a communication channel for a user
     * @param canvasUserId The canvas user id
     * @param communicationChannel Wrapped CommunicationChannel to create
     * @return The created CommunicationChannel
     */
    public CommunicationChannel createCommunicationChannel(String canvasUserId, CommunicationChannelCreateWrapper communicationChannel) {
        URI uri = CC_BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasUserId);
        log.debug("{}", uri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        HttpEntity<CommunicationChannelCreateWrapper> requestEntity = new HttpEntity<>(communicationChannel, headers);

        try {
            return this.restTemplate.postForObject(uri, requestEntity, CommunicationChannel.class);
        } catch (HttpClientErrorException hcee) {
            log.error("Failed to create communication channel for user " + canvasUserId, hcee);
        }
        return null;
    }

    /**
     * Build a new CommunicationChannelCreateWrapper object
     * @param canvasUserId The canvas user id
     * @param email Email address to use
     * @param type Type of channel
     * @return A new CommunicationChannelCreateWrapper object
     */
    public CommunicationChannelCreateWrapper buildCommunicationChannelWrapper(String canvasUserId, String email, String type) {
        CommunicationChannel newChannel = new CommunicationChannel();
        newChannel.setUserId(canvasUserId);
        newChannel.setAddress(email);
        newChannel.setType(type);

        CommunicationChannelCreateWrapper channelWrapper = new CommunicationChannelCreateWrapper();
        channelWrapper.setSkipConfirmation(true);
        channelWrapper.setCommunicationChannel(newChannel);
        return channelWrapper;
    }
}
