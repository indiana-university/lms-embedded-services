package edu.iu.uits.lms.canvas.services;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2024 Indiana University
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

import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.Module;
import edu.iu.uits.lms.canvas.model.ModuleCreateWrapper;
import edu.iu.uits.lms.canvas.model.ModuleItem;
import edu.iu.uits.lms.canvas.model.ModuleItemCreateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.List;

@Service
@Slf4j
public class ModuleService extends SpringBaseService {

    private static final String MODULES_BASE_URI = "{url}/courses/{course_id}/modules";
    private static final String MODULES_URI = MODULES_BASE_URI + "/{id}";
    private static final String MODULE_ITEM_BASE_URI = MODULES_URI + "/items";

    private static final UriTemplate MODULES_BASE_TEMPLATE = new UriTemplate(MODULES_BASE_URI);
    private static final UriTemplate MODULES_TEMPLATE = new UriTemplate(MODULES_URI);
    private static final UriTemplate MODULE_ITEMS_TEMPLATE = new UriTemplate(MODULE_ITEM_BASE_URI);

    /**
     * Get all course modules
     * @param courseId Course id
     * @param searchTerm Search term that matches on the module name (optional)
     * @return List of Module items
     */
    public List<Module> getModules(String courseId, String searchTerm) {
        URI uri = MODULES_BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        if (searchTerm != null) {
            builder.queryParam("search_term", searchTerm);
        }

        builder.queryParam("per_page", "100");

        return doGet(builder.build().toUri(), Module[].class);
    }

    /**
     * Create a module in a course
     * @param courseId Course id
     * @param newModule Wrapper object used to create the module
     * @param asUser optional - masquerade as this user when creating the module. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1).
     *               Send in null to not set the user explicitly (api caller owner will own)
     * @return Created Module
     */
    public Module createModule(String courseId, ModuleCreateWrapper newModule, String asUser) {
        Module savedModule = null;

        URI uri = MODULES_BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        if (asUser != null) {
            builder.queryParam("as_user_id", asUser);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<ModuleCreateWrapper> request = new HttpEntity<>(newModule, headers);
            ResponseEntity<Module> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, request, Module.class);
            log.debug("{}", response);

            savedModule = response.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating module", hcee);
            throw new RuntimeException("Error creating module", hcee);
        }

        return savedModule;
    }

    /**
     * Update an existing course module
     * @param courseId Course id
     * @param moduleId Module id
     * @param newModule Module wrapper
     * @return Updated Module
     */
    public Module updateModule(String courseId, String moduleId, ModuleCreateWrapper newModule) {
        Module savedModule = null;

        URI uri = MODULES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, moduleId);
        log.debug("{}", uri);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<ModuleCreateWrapper> request = new HttpEntity<>(newModule, headers);
            ResponseEntity<Module> response = this.restTemplate.exchange(uri, HttpMethod.PUT, request, Module.class);
            log.debug("{}", response);

            savedModule = response.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error updating module", hcee);
            throw new RuntimeException("Error updating module", hcee);
        }

        return savedModule;
    }

    /**
     * Publish/unpublish an existing course module
     * @param courseId Course id
     * @param moduleId Module id
     * @param published Flag indicating of the module should be published or unpublished
     * @return Updated module
     */
    public Module publishModule(String courseId, String moduleId, boolean published) {
        Module savedModule = null;

        URI uri = MODULES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, moduleId);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("module[published]", published);

        log.debug("{}", uri);

        try {
            ResponseEntity<Module> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, Module.class);
            log.debug("{}", response);

            savedModule = response.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating module", hcee);
            throw new RuntimeException("Error creating module", hcee);
        }

        return savedModule;
    }

    /**
     * Get all module items for a given module
     * @param courseId Course id
     * @param moduleId Module id
     * @param searchTerm Search term that matches on the module item name (optional)
     * @return List of ModuleItem items
     */
    public List<ModuleItem> getModuleItems(String courseId, String moduleId, String searchTerm) {
        URI uri = MODULE_ITEMS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, moduleId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        if (searchTerm != null) {
            builder.queryParam("search_term", searchTerm);
        }

        builder.queryParam("per_page", "100");

        return doGet(builder.build().toUri(), ModuleItem[].class);
    }

    /**
     * Create module item in a given course module
     * @param courseId Course id
     * @param moduleId Module id
     * @param newModuleItem Wrapper object used to create the module item
     * @param asUser optional - masquerade as this user when creating the module item. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
     *               Send in null to not set the user explicitly (api caller owner will own)
     * @return Created ModuleItem
     */
    public ModuleItem createModuleItem(String courseId, String moduleId, ModuleItemCreateWrapper newModuleItem, String asUser) {
        ModuleItem savedModuleItem = null;

        URI uri = MODULE_ITEMS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, moduleId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        if (asUser != null) {
            builder.queryParam("as_user_id", asUser);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<ModuleItemCreateWrapper> request = new HttpEntity<>(newModuleItem, headers);
            ResponseEntity<ModuleItem> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, request, ModuleItem.class);
            log.debug("{}", response);

            savedModuleItem = response.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating module item", hcee);
            throw new RuntimeException("Error creating module item", hcee);
        }

        return savedModuleItem;
    }

    /**
     * Get the first matching module
     * @param courseId Course id
     * @param moduleName Module name to look up
     * @return First matching Module (or null, if none found)
     */
    public Module getModuleByName(String courseId, String moduleName) {
        List<Module> modules = getModules(courseId, moduleName);
        return modules.stream().filter(m -> moduleName.equals(m.getName())).findFirst().orElse(null);
    }

    /**
     * Get the first matching module item
     * @param courseId Course id
     * @param moduleId Module id
     * @param moduleItemTitle ModuleItem title to look up
     * @return First matching ModuleItem (or null, if none found)
     */
    public ModuleItem getModuleItemByTitle(String courseId, String moduleId, String moduleItemTitle) {
        List<ModuleItem> moduleItems = getModuleItems(courseId, moduleId, moduleItemTitle);
        return moduleItems.stream().filter(m -> moduleItemTitle.equals(m.getTitle())).findFirst().orElse(null);
    }

}
