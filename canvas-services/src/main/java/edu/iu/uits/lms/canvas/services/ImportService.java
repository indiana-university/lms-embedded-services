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

import edu.iu.uits.lms.canvas.model.uploadstatus.CanvasUploadStatus;
import edu.iu.uits.lms.canvas.model.uploadstatus.CanvasUploadStatusWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chmaurer on 4/29/15.
 */
@Service
@Slf4j
public class ImportService extends SpringBaseService {

    private static final String BASE_URI = "{url}/accounts/{accountId}";
    private static final String UPLOAD_URI = BASE_URI + "/sis_imports.json";
    private static final String SIS_IMPORT_BASE = BASE_URI + "/sis_imports";
    private static final String SIS_IMPORT = SIS_IMPORT_BASE + "/{id}";

    private static final UriTemplate UPLOAD_TEMPLATE = new UriTemplate(UPLOAD_URI);
    private static final UriTemplate SIS_IMPORTS_TEMPLATE = new UriTemplate(SIS_IMPORT_BASE);
    private static final UriTemplate SIS_IMPORT_TEMPLATE = new UriTemplate(SIS_IMPORT);


    /**
     * Use to send a csv file to Canvas.
     *
     * @param file file
     * @return The id of the import
     */
    public String sendCsvToCanvas(MultipartFile file) {
        try {
            return sendFileToCanvas(file.getBytes(), "instructure_csv", "csv", false);
        } catch (IOException e) {
            log.error("unable to send file to canvas", e);
        }
        return null;
    }

    /**
     * Use to send a zip file to Canvas.
     *
     * @param bytes file bytes
     * @return The id of the import
     */
    public String sendZipToCanvas(byte[] bytes) {
        return sendFileToCanvas(bytes, "instructure_csv", "zip", false);
    }

    public String sendZipToCanvasOverrideStickiness(byte[] bytes) {
        return sendFileToCanvas(bytes, "instructure_csv", "zip", true);
    }

    /**
     * Send either a csv or zip to canvas
     * @param fileBytes File bytes
     * @param importType Generally, will be "instructure_csv"
     * @param extension Likely zip or csv
     * @return The import id created
     */
    private String sendFileToCanvas(byte[] fileBytes, String importType, String extension, boolean overrideStickiness) {
        URI uri = UPLOAD_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasConfiguration.getAccountId());
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("import_type", importType);
        builder.queryParam("extension", extension);

        if (overrideStickiness) {
            builder.queryParam("override_sis_stickiness", true);
            builder.queryParam("clear_sis_stickiness", true);
        }

        HttpHeaders headers = new HttpHeaders();
        try {
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);
            CanvasUploadStatus sisImport = this.restTemplate.postForObject(builder.build().toUri(), requestEntity, CanvasUploadStatus.class);
            return sisImport.getId();
        } catch (Exception e) {
            log.error("unable to send file to canvas", e);
        }

        return null;
    }

    /**
     * Get the status of the given import
     * @param importId The id of the import to check
     * @return CanvasUploadStatus
     */
    public CanvasUploadStatus getImportStatus(String importId) {
        URI uri = SIS_IMPORT_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(),
              canvasConfiguration.getAccountId(), importId);
        log.debug("{}", uri);

        try {
            ResponseEntity<CanvasUploadStatus> entity = this.restTemplate.getForEntity(uri, CanvasUploadStatus.class);
            log.debug("{}", entity);

            return entity.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error getting the import status for " + importId, hcee);
        }

        return null;
    }

    /**
     * Get the statuses of all imports that match the given criteria
     * @param createdSince
     *      To go from a date to an appropriate string, use something like this:
     *      CanvasDateFormatUtil.formatDateForDisplay(createdSince, null, CanvasDateFormatUtil.CANVAS_DATE_FORMAT)
     * @param workflowStates List of workflow states to filter on
     * @return All matching CanvasUploadStatus objects
     */
    public List<CanvasUploadStatus> getImports(String createdSince, List<String> workflowStates) {
        URI uri = SIS_IMPORTS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(),
              canvasConfiguration.getAccountId());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        if (createdSince != null) {
            builder.queryParam("created_since", createdSince);
        }

        if (workflowStates != null && !workflowStates.isEmpty()) {
            for (String state : workflowStates) {
                builder.queryParam("workflow_state[]", state);
            }
        }

        log.debug("{}", builder.build().toUri());

        List<CanvasUploadStatusWrapper> sisImports = doGetSingle(builder.build().toUri(), CanvasUploadStatusWrapper.class);

        List<CanvasUploadStatus> imports = new ArrayList<>();

        if (sisImports != null && !sisImports.isEmpty()) {
            sisImports.stream().map(CanvasUploadStatusWrapper::getSisImports)
                  .filter(sisImports1SisImports -> !sisImports1SisImports.isEmpty())
                  .forEach(imports::addAll);
        } else {
            log.warn("No sis imports returned.");
        }

        return imports;
    }

    /**
     * Convert the file at the filePath to a byte[]
     * @param filePath Patah to file
     * @return byte[]
     */
    private byte[] readArchive(String filePath) {
        File file = new File(filePath);

        byte[] result = new byte[(int) file.length()];
        InputStream input = null;
        try {
            int totalBytesRead = 0;
            input = new BufferedInputStream(new FileInputStream(file));
            while (totalBytesRead < result.length) {
                int bytesRemaining = result.length - totalBytesRead;
                int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                if (bytesRead > 0) {
                    totalBytesRead = totalBytesRead + bytesRead;
                }
            }
        } catch (IOException e) {
            log.error("Error reading archive", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("Error closing stream", e);
                }
            }
        }
        return result;
    }
}
