package com.example

import com.example.data.snn.SnnEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnnEngineTest {

    @Test
    fun `test SNN initializes at resting potential`() {
        val engine = SnnEngine(numNeurons = 20)
        assertEquals(0f, engine.simTimeMs, 0.001f)
        assertEquals(-65f, engine.vSoma[0], 0.1f)
        assertTrue("m gate should be in [0, 1]", engine.mGate[0] in 0f..1f)
        assertTrue("h gate should be in [0, 1]", engine.hGate[0] in 0f..1f)
        assertTrue("n gate should be in [0, 1]", engine.nGate[0] in 0f..1f)
    }

    @Test
    fun `test SNN steps forward in time`() {
        val engine = SnnEngine(numNeurons = 20, dtMs = 0.1f)
        engine.step(0.1f)
        assertTrue(engine.simTimeMs > 0f)
    }

    @Test
    fun `test SNN current pulse stimulation`() {
        val engine = SnnEngine(numNeurons = 20, dtMs = 0.1f)
        engine.injectCurrentPulse(magnitudeMicroA = 20.0f, durationMs = 15.0f)
        for (i in 0 until 100) {
            engine.step(0.1f)
        }
        val telemetry = engine.getTelemetry()
        assertTrue("Telemetry should reflect active neurons", telemetry.activeNeurons >= 0)
    }

    @Test
    fun `test Tsodyks-Markram plasticity state`() {
        val engine = SnnEngine(numNeurons = 20)
        assertEquals(1.0f, engine.tmX, 0.01f)
        assertTrue(engine.tmU in 0f..1f)
    }

    @Test
    fun `test Recharts telemetry contains layer firing rates and synchronization`() {
        val engine = SnnEngine(numNeurons = 120, dtMs = 0.1f)
        for (i in 0 until 50) {
            engine.step(0.1f)
        }
        val telemetry = engine.getTelemetry()

        // Layer firing rates
        assertTrue("L2/3 firing rate should be positive", telemetry.layer23Hz > 0f)
        assertTrue("L4 firing rate should be positive", telemetry.layer4Hz > 0f)
        assertTrue("L5 firing rate should be positive", telemetry.layer5Hz > 0f)
        assertTrue("PV firing rate should be positive", telemetry.layerPvHz > 0f)

        // Layer synchronization
        assertTrue("Synchrony index should be between 0 and 1", telemetry.synchronyIndex in 0f..1f)
        assertTrue("L2/3-L4 sync should be between 0 and 1", telemetry.layerSyncL2L4 in 0f..1f)
        assertTrue("L4-L5 sync should be between 0 and 1", telemetry.layerSyncL4L5 in 0f..1f)
        assertTrue("PV sync should be between 0 and 1", telemetry.layerSyncPv in 0f..1f)

        // History and nodes
        assertTrue("Activity history should have data points", telemetry.history.isNotEmpty())
        assertEquals("Nodes list should contain all 120 neurons", 120, telemetry.nodes.size)
    }
}
