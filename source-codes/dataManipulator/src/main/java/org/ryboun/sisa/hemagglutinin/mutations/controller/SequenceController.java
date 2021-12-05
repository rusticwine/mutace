package org.ryboun.sisa.hemagglutinin.mutations.controller;

import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("sequence")
public class SequenceController {

    @Autowired
    SequenceService sequenceService;

    @GetMapping
    public List<Sequence> getAllSequences() {
        return sequenceService.findAllSequences();
    }
}
