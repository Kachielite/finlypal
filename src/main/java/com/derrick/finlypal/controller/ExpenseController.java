package com.derrick.finlypal.controller;

import com.derrick.finlypal.dto.ErrorResponseDTO;
import com.derrick.finlypal.dto.ExpenseRequestDTO;
import com.derrick.finlypal.dto.ExpenseResponseDTO;
import com.derrick.finlypal.dto.GeneralResponseDTO;
import com.derrick.finlypal.enums.ExpenseType;
import com.derrick.finlypal.exception.BadRequestException;
import com.derrick.finlypal.exception.InternalServerErrorException;
import com.derrick.finlypal.exception.NotAuthorizedException;
import com.derrick.finlypal.exception.NotFoundException;
import com.derrick.finlypal.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/expenses")
@Tag(name = "Expenses", description = "Manage user's expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/id/{expense_id}")
    @Operation(
            summary = "Retrieve Expense Details",
            description = "Retrieve a specific expense by its unique identifier, ensuring that the user has the necessary authorization to access the requested data. This operation involves validating the user's permissions, fetching the expense from the database, and returning it in a structured response format."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense details fetched successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expense not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(
            @NotEmpty(message = "Expense id cannot be empty")
            @PathVariable Long expense_id
    ) throws NotFoundException, InternalServerErrorException, NotAuthorizedException {
        return new ResponseEntity<>(
                expenseService.findById(expense_id),
                HttpStatus.OK
        );
    }

    @GetMapping
    @Operation(
            summary = "Retrieve All Expenses",
            description = "Retrieve a comprehensive list of all expenses associated with the currently logged-in user. This operation involves accessing the user's expense records from the database, organizing them in a paginated format, and returning them in a structured response. The user must be authenticated and authorized to view this data, ensuring secure access to sensitive financial information."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expenses fetched successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public ResponseEntity<Page<ExpenseResponseDTO>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) throws InternalServerErrorException {
        return new ResponseEntity<>(
                expenseService.findAllByUserId(page, pageSize),
                HttpStatus.OK
        );
    }

    @GetMapping("/category/{category_id}")
    @Operation(
            summary = "Retrieve Expenses by Category",
            description = "Retrieve a list of expenses associated with a specific category ID for the currently logged-in user. This operation ensures that the user is authenticated and authorized to access the data. It fetches the expenses from the database, organizes them into a paginated format, and returns them in a structured response."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expenses fetched successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public ResponseEntity<Page<ExpenseResponseDTO>> getExpensesByCategoryId(
            @NotEmpty(message = "Category id cannot be empty")
            @PathVariable Long category_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) throws InternalServerErrorException {
        return new ResponseEntity<>(
                expenseService.findAllByCategoryIdAndUserId(category_id, page, pageSize),
                HttpStatus.OK
        );
    }

    @GetMapping("/dates")
    @Operation(
            summary = "Get expenses by category",
            description = "Fetch a list of expenses associated with a specific category ID for the currently logged-in user. This operation ensures that the user is authenticated and authorized to access the data. It fetches the expenses from the database, organizes them into a paginated format, and returns them in a structured response."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expenses fetched successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public ResponseEntity<Page<ExpenseResponseDTO>> getExpensesByCategoryId(
            @RequestParam LocalDate start_date,
            @RequestParam LocalDate end_date,
            @RequestParam int page,
            @RequestParam int pageSize
    ) throws BadRequestException, InternalServerErrorException {
        return new ResponseEntity<>(
                expenseService.findAllByDateBetween(start_date, end_date, page, pageSize),
                HttpStatus.OK
        );
    }

    @GetMapping("/types/{type}")
    @Operation(
            summary = "Fetch expenses by expense type and dates for the logged-in user",
            description = "This operation fetches expenses by expense type and dates for the logged-in user. It ensures that the user is authenticated and authorized to access the data. It fetches the expenses from the database, organizes them into a paginated format, and returns them in a structured response. The response includes the total count of expenses, the page size, and the list of expenses."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expenses fetched successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public ResponseEntity<Page<ExpenseResponseDTO>> getExpensesByTypeAndDate(
            @NotEmpty(message = "Expense type cannot be empty")
            @PathVariable ExpenseType type,
            @RequestParam(required = false) Optional<LocalDate> start_date,
            @RequestParam(required = false) Optional<LocalDate> end_date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) throws BadRequestException, InternalServerErrorException {
        return new ResponseEntity<>(
                expenseService.findAllByTypeAndUserIdOrDateBetween(
                        type,
                        start_date.orElse(null),
                        end_date.orElse(null),
                        page,
                        pageSize
                ),
                HttpStatus.OK
        );
    }

    @PostMapping
    @Operation(
            summary = "Create new expense",
            description = "This operation creates a new expense for the logged-in user. It ensures that the user is authenticated and authorized to access the data. It creates a new expense in the database, maps the new expense to the response format, and returns it in a structured response. The response includes the expense's ID, description, amount, type, category ID, and user ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Expense created successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public ResponseEntity<GeneralResponseDTO> createExpense(
            @Valid @RequestBody ExpenseRequestDTO expense
    ) throws InternalServerErrorException, BadRequestException {
        return new ResponseEntity<>(
                expenseService.addExpense(expense),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{expense_id}")
    @Operation(
            summary = "Update expense",
            description = """
                    Modify existing expense
                    
                    This operation modifies an existing expense in the database. It ensures that the user is authenticated and authorized to access the data. It updates the expense in the database, maps the new expense to the response format, and returns it in a structured response. The response includes the expense's ID, description, amount, type, category ID, and user ID.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense updated successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public ResponseEntity<GeneralResponseDTO> updateExpense(
            @NotEmpty(message = "Expense id cannot be empty")
            @PathVariable Long expense_id,
            @RequestBody ExpenseRequestDTO expense
    ) throws NotFoundException, InternalServerErrorException, NotAuthorizedException {
        return new ResponseEntity<>(
                expenseService.updateExpense(expense_id, expense),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{expense_id}")
    @Operation(
            summary = "Delete expense",
            description = """
                    Delete expense by id
                    
                    This operation deletes an existing expense by its ID. It ensures that the user is authenticated and authorized to access the data. It deletes the expense in the database, and returns a success response.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public ResponseEntity<GeneralResponseDTO> deleteExpense(
            @NotEmpty(message = "Expense id cannot be empty")
            @PathVariable Long expense_id
    ) throws InternalServerErrorException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity<>(
                expenseService.deleteExpense(expense_id),
                HttpStatus.OK
        );
    }

}
