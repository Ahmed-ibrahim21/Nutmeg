package com.wr.nutmeg.manager;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.manager.dtos.AssignmentResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final ClubRepository clubRepository;

    public ManagerService(ManagerRepository managerRepository, ClubRepository clubRepository) {
        this.managerRepository = managerRepository;
        this.clubRepository = clubRepository;
    }

    @Transactional
    public AssignmentResult assignClub(UUID managerId, UUID clubId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + managerId));

        if (manager.getClub() != null) {
            throw new IllegalStateException("Manager already manages a club: " + manager.getClub().getName());
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));

        if (club.getManager() != null) {
            throw new IllegalStateException("Club already has a manager: " + club.getManager().getUsername());
        }

        manager.setClub(club);
        managerRepository.save(manager);

        return new AssignmentResult(manager.getId(), manager.getUsername(), club.getId(), club.getName());
    }

}
