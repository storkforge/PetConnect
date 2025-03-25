package se.storkforge.petconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.service.MeetUpService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/meetups")
public class MeetUpController {
    @Autowired
    private MeetUpService meetUpService;

    @GetMapping("/search")
    public List<MeetUp> searchMeetUps(
            @RequestParam String location,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return meetUpService.searchMeetUps(location, start, end);
    }
}
