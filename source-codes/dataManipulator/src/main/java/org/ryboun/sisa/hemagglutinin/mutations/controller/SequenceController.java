package org.ryboun.sisa.hemagglutinin.mutations.controller;

import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
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

//    AlignedSe

    @GetMapping
    public List<Sequence> getAllSequences() {
        return sequenceService.findAllSequences();
    }

    @GetMapping(path = "/aligned")
    public List<AlignedSequence> getAllAlignedSequences() {
        return sequenceService.findAllAlignedSequences();
    }
}
