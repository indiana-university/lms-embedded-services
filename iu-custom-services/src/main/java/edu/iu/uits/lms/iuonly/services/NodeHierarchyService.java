package edu.iu.uits.lms.iuonly.services;

/*-
 * #%L
 * lms-canvas-iu-custom-services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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

import edu.iu.uits.lms.canvas.model.Account;
import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.iuonly.model.nodehierarchy.HierarchyNode;
import edu.iu.uits.lms.iuonly.model.nodehierarchy.NodeWrapper;
import edu.iu.uits.lms.iuonly.repository.NodeHierarchyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NodeHierarchyService {
    @Autowired
    private NodeHierarchyRepository nodeHierarchyRepository;

    @Autowired
    private CanvasService canvasService;

    /**
     * Return the hierarchy based off of canvas accounts
     * @param accounts
     * @return
     */
    public List<HierarchyNode> buildHierarchyForAllAccounts(List<Account> accounts) {
        Map<String, List<HierarchyNode>> childrenForParentHierarchyMap = new HashMap<>();
        Map<String, HierarchyNode> hierarchyMap = new HashMap<>();

        final String rootAccountId = "root";

        Account rootAccount = new Account();
        rootAccount.setId(canvasService.getRootAccount());
        rootAccount.setName("Indiana University");
        rootAccount.setParentAccountId(rootAccountId);

        accounts.add(rootAccount);

        for (Account account : accounts) {
            HierarchyNode hierarchyNode = new HierarchyNode();
            hierarchyNode.setId(account.getId());
            hierarchyNode.setName(account.getName());

            String parentId = account.getParentAccountId();

            if (! childrenForParentHierarchyMap.containsKey(parentId)) {
                childrenForParentHierarchyMap.put(parentId, new ArrayList<>());
            }

            hierarchyMap.put(account.getId(), hierarchyNode);
            childrenForParentHierarchyMap.get(parentId).add(hierarchyNode);
        }

        for (HierarchyNode hierarchyNode : hierarchyMap.values()) {
            String id = hierarchyNode.getId();
            List<HierarchyNode> childrenList = childrenForParentHierarchyMap.get(id) == null ? new ArrayList<>() : childrenForParentHierarchyMap.get(id);

            childrenList = childrenList.stream()
                    .sorted(Comparator.comparing(HierarchyNode::getName))
                    .collect(Collectors.toList());

            hierarchyNode.setChildren(childrenList);
        }

        // return root node list
        return childrenForParentHierarchyMap.get(rootAccountId);
    }

    public String writeHierarchy(List<HierarchyNode> hierarchyNodes) {
        nodeHierarchyRepository.deleteAll();
        NodeWrapper nodeWrapper = new NodeWrapper(hierarchyNodes);
        nodeHierarchyRepository.save(nodeWrapper);

        return "Hierarchy created";
    }

    public List<HierarchyNode> readHierarchy() {
        NodeWrapper nodeWrapper = nodeHierarchyRepository.findTop1ByOrderByModifiedDesc();

        return nodeWrapper.getNodeHierarchy();
    }

    public List<String> getFlattenedHierarchy() {
        List<String> flattened = new ArrayList<>();
        List<HierarchyNode> hierarchy = readHierarchy();

        flattened.addAll(recursiveFlatten(hierarchy));

        return flattened;
    }

    private List<String> recursiveFlatten(List<HierarchyNode> hierarchyNodes) {
        List<String> flattened = new ArrayList<>();

        for (HierarchyNode hierarchyNode : hierarchyNodes) {
            flattened.add(hierarchyNode.getName());
            flattened.addAll(recursiveFlatten(hierarchyNode.getChildren()));
        }

        return flattened;
    }

}
