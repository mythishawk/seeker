package com.job.seeker.discovery;

import com.job.seeker.domain.SourceType;
import java.util.List;

/** Reads the open jobs of one company from one kind of job board. */
public interface JobBoardClient {

    SourceType type();

    /** Returns every open job on the company's board. Throws BoardFetchException if the board cannot be read. */
    List<FetchedJob> fetch(String boardSlug);
}
