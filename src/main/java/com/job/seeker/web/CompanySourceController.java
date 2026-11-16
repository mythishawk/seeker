package com.job.seeker.web;

import com.job.seeker.domain.CompanySource;
import com.job.seeker.domain.SourceType;
import com.job.seeker.repository.CompanySourceRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The watchlist: which company job boards the agent reads. */
@RestController
@RequestMapping("/api/sources")
public class CompanySourceController {

    public record SourceView(Long id, String company, SourceType sourceType, String boardSlug, boolean active,
                             Instant lastCheckedAt, String lastError, int consecutiveFailures) {
        static SourceView from(CompanySource s) {
            return new SourceView(s.getId(), s.getCompany(), s.getSourceType(), s.getBoardSlug(), s.isActive(),
                    s.getLastCheckedAt(), s.getLastError(), s.getConsecutiveFailures());
        }
    }

    public record AddSourceRequest(@NotBlank String company, @NotNull SourceType sourceType, @NotBlank String boardSlug) {
    }

    public record ActiveRequest(boolean active) {
    }

    private final CompanySourceRepository repository;

    public CompanySourceController(CompanySourceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SourceView> list() {
        return repository.findAll().stream().map(SourceView::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SourceView add(@Valid @RequestBody AddSourceRequest request) {
        String slug = request.boardSlug().trim();
        if (repository.existsBySourceTypeAndBoardSlug(request.sourceType(), slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That board is already on the watchlist");
        }
        CompanySource source = new CompanySource();
        source.setCompany(request.company().trim());
        source.setSourceType(request.sourceType());
        source.setBoardSlug(slug);
        source.setActive(true);
        return SourceView.from(repository.save(source));
    }

    @PatchMapping("/{id}")
    @Transactional
    public SourceView setActive(@PathVariable Long id, @RequestBody ActiveRequest request) {
        CompanySource source = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such source"));
        source.setActive(request.active());
        return SourceView.from(source);
    }
}
