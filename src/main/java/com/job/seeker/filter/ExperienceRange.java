package com.job.seeker.filter;

/** Years of experience a job asks for. {@code max} is null for open-ended asks like "5+ years". */
public record ExperienceRange(Integer min, Integer max) {
}
