package se.storkforge.petconnect.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.service.PetService;
import se.storkforge.petconnect.service.UserService;

@Controller
@RequestMapping("/pets/web")
public class PetWebController {

    private final PetService petService;
    private final UserService userService;

    public PetWebController(PetService petService, UserService userService) {
        this.petService = petService;
        this.userService = userService;
    }

    @GetMapping("/add")
    public String showAddPetForm(Model model) {
        model.addAttribute("pet", new PetInputDTO());
        return "pets/addPetForm";
    }

    @PostMapping("/delete/{id}")
    public String deletePet(@PathVariable Long id, Authentication auth) {
        petService.deletePet(id, auth.getName());
        return "redirect:/profile/" + auth.getName();
    }

    @PostMapping("/add")
    public String handlePetForm(
            @ModelAttribute PetInputDTO petInput,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            Authentication auth) {

        userService.getUserByUsername(auth.getName()).ifPresent(user -> {
            petInput.setOwnerId(user.getId());
        });

        Pet createdPet = petService.createPet(petInput, auth.getName());

        if (imageFile != null && !imageFile.isEmpty()) {
            petService.uploadProfilePicture(createdPet.getId(), imageFile);
        }

        return "redirect:/profile/" + auth.getName();
    }
}
