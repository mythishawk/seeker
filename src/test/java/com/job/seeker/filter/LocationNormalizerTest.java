package com.job.seeker.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class LocationNormalizerTest {

    private final LocationNormalizer normalizer = LocationNormalizer.fromClasspath();

    @Test
    void cityWithStateCode() {
        NormalizedLocation loc = normalizer.normalize("Pune, MH");
        assertEquals(Set.of("Pune"), loc.cities());
        assertEquals("Pune", loc.canonical());
        assertTrue(loc.indiaSignal());
        assertFalse(loc.remote());
    }

    @Test
    void aliasesMapToOneCity() {
        assertEquals(Set.of("Bengaluru"), normalizer.normalize("Bangalore").cities());
        assertEquals(Set.of("Bengaluru"), normalizer.normalize("BLR").cities());
        assertEquals("Bengaluru", normalizer.normalize("Bengaluru, Karnataka, India").canonical());
        assertEquals(Set.of("Mumbai"), normalizer.normalize("Navi Mumbai").cities());
        assertEquals(Set.of("Delhi NCR"), normalizer.normalize("Gurgaon").cities());
        assertEquals(Set.of("Delhi NCR"), normalizer.normalize("New Delhi").cities());
    }

    @Test
    void severalCitiesAreAllFound() {
        NormalizedLocation loc = normalizer.normalize("Pune; Hyderabad");
        assertEquals(Set.of("Hyderabad", "Pune"), loc.cities());
        assertEquals("Hyderabad, Pune", loc.canonical());
    }

    @Test
    void remoteIndia() {
        NormalizedLocation loc = normalizer.normalize("Remote - India");
        assertTrue(loc.remote());
        assertTrue(loc.indiaMentioned());
        assertTrue(loc.cities().isEmpty());
        assertEquals("Remote - India", loc.canonical());
    }

    @Test
    void indiaAlone() {
        NormalizedLocation loc = normalizer.normalize("India");
        assertTrue(loc.indiaSignal());
        assertFalse(loc.remote());
        assertEquals("India", loc.canonical());
    }

    @Test
    void plainRemoteAndForeignRemote() {
        NormalizedLocation remote = normalizer.normalize("Remote");
        assertTrue(remote.remote());
        assertFalse(remote.indiaSignal());
        assertEquals("Remote", remote.canonical());

        NormalizedLocation us = normalizer.normalize("Remote - US");
        assertTrue(us.remote());
        assertEquals(Set.of("US"), us.foreignCountries());
        assertEquals("Remote - US", us.canonical());
    }

    @Test
    void foreignCities() {
        NormalizedLocation loc = normalizer.normalize("London, UK");
        assertEquals(Set.of("GB"), loc.foreignCountries());
        assertFalse(loc.indiaSignal());
        assertTrue(loc.recognized());
    }

    @Test
    void hybridIsNotRemote() {
        NormalizedLocation loc = normalizer.normalize("Hybrid - Pune");
        assertFalse(loc.remote());
        assertEquals(Set.of("Pune"), loc.cities());
    }

    @Test
    void unknownPlaceIsNotRecognized() {
        NormalizedLocation loc = normalizer.normalize("Salt Lake City, UT");
        assertFalse(loc.recognized());
        assertEquals("Salt Lake City, UT", loc.canonical());
    }

    @Test
    void blankAndNull() {
        assertTrue(normalizer.normalize(null).blank());
        assertTrue(normalizer.normalize("   ").blank());
    }
}
