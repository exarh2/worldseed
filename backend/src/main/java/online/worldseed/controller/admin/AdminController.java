package online.worldseed.generator.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import online.worldseed.generator.model.dto.admin.DropAllTerrainsResult;
import online.worldseed.generator.service.admin.AdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "API администрирования")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(path = "/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Удаление всех террейнов",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Террейны успешно удалены")
            })
    @PostMapping("/drop-all-terrains")
    public DropAllTerrainsResult dropAllTerrains() {
        if (adminService.getDropInProgress()) {
            return DropAllTerrainsResult.builder().inProgress(true).build();
        }
        adminService.dropAllTerrains();
        return DropAllTerrainsResult.builder().inProgress(false).build();
    }
}
