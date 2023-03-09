package org.avni.messaging.controller;

import org.avni.messaging.contract.glific.GlificContactResponse;
import org.avni.messaging.contract.glific.Message;
import org.avni.messaging.domain.exception.GlificContactNotFoundError;
import org.avni.messaging.repository.GlificContactRepository;
import org.avni.messaging.service.PhoneNumberNotAvailableException;
import org.avni.server.dao.UserRepository;
import org.avni.server.domain.User;
import org.avni.server.service.IndividualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
public class ContactController {
    private static final String ContactEndpoint = "/web/contact";
    private final GlificContactRepository glificContactRepository;
    private final IndividualService individualService;
    private final UserRepository userRepository;

    @Autowired
    public ContactController(GlificContactRepository glificContactRepository, IndividualService individualService, UserRepository userRepository) {
        this.glificContactRepository = glificContactRepository;
        this.individualService = individualService;
        this.userRepository = userRepository;
    }

    @GetMapping(ContactEndpoint + "/subject/{id}")
    @PreAuthorize(value = "hasAnyAuthority('user')")
    public GlificContactResponse fetchContactSubject(@PathVariable("id") long subjectId) throws GlificContactNotFoundError, PhoneNumberNotAvailableException {
        String phoneNumber = individualService.fetchIndividualPhoneNumber(subjectId);
        return glificContactRepository.findContact(phoneNumber);
    }

    @GetMapping(ContactEndpoint + "/user/{id}")
    @PreAuthorize(value = "hasAnyAuthority('user')")
    public GlificContactResponse fetchContactUser(@PathVariable("id") long userId) throws GlificContactNotFoundError {
        User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        return glificContactRepository.findContact(user.getPhoneNumber());
    }

    @GetMapping(ContactEndpoint + "/subject/{id}/msgs")
    @PreAuthorize(value = "hasAnyAuthority('user')")
    public ResponseEntity<List<Message>> fetchAllMsgsForContactSubject(@PathVariable("id") long subjectId) {
        String phoneNumber;
        try {
            phoneNumber = individualService.fetchIndividualPhoneNumber(subjectId);
        } catch (PhoneNumberNotAvailableException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(glificContactRepository.getAllMsgsForContact(phoneNumber));
    }

    @GetMapping(ContactEndpoint + "/user/{id}/msgs")
    @PreAuthorize(value = "hasAnyAuthority('user')")
    public List<Message> fetchAllMsgsForContactUser(@PathVariable("id") long userId) {
        User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        return glificContactRepository.getAllMsgsForContact(user.getPhoneNumber());
    }
}
