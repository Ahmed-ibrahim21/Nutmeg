package com.wr.nutmeg.manager;
import com.wr.nutmeg.auth.ManagerUserDetails;
import com.wr.nutmeg.manager.dtos.AssignClubRequest;
import com.wr.nutmeg.manager.dtos.AssignmentResult;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/managers")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @PostMapping("/assign-club")
    public AssignmentResult assignClub(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @Valid @RequestBody AssignClubRequest request
    ) {
        return managerService.assignClub(principal.getId(), request.clubId());
    }

}
