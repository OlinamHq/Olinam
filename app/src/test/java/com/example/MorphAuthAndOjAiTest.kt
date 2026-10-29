package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.OjAiAgentManager
import com.example.ai.OjAiService
import com.example.data.MorphRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MorphAuthAndOjAiTest {

    private lateinit var context: Context
    private lateinit var morphRepo: MorphRepository
    private lateinit var agentManager: OjAiAgentManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        morphRepo = MorphRepository.getInstance(context)
        morphRepo.disconnectAccount()
        agentManager = OjAiAgentManager(context)
    }

    @Test
    fun `unauthenticated morph blocks dummy toggle and prompts sign-in in Oj AI`() = runBlocking {
        assertNull(morphRepo.connectedAccountFlow.value)
        assertTrue(morphRepo.notesFlow.value.isEmpty())

        // Trying to enable morph_plugin without signing into an account must return false
        val enabled = agentManager.togglePlugin("morph_plugin", true)
        assertFalse(enabled)

        val aiReply = OjAiService.generateResponse(
            userPrompt = "Show my Morph notes",
            history = emptyList(),
            agentManager = agentManager
        )
        assertTrue(aiReply.contains("not signed in", ignoreCase = true) || aiReply.contains("Sign in", ignoreCase = true))
    }

    @Test
    fun `per-account isolation and live Oj AI Morph create-read-delete flow`() = runBlocking {
        // 1. Sign in as Account A
        val accountAResult = morphRepo.signInWithOmkunCredentials(
            email = "rahul@omkun.com",
            password = "StrongPassword123",
            displayName = "Rahul Sharma"
        )
        assertTrue(accountAResult.isSuccess)
        val accountA = accountAResult.getOrThrow()
        assertEquals("rahul@omkun.com", accountA.email)
        assertTrue(agentManager.togglePlugin("morph_plugin", true))

        // 2. Ask Oj AI to create a note in Rahul's Morph account
        val createReply = OjAiService.generateResponse(
            userPrompt = "Save a note in Morph titled Interior Quote: Modular kitchen ₹2.4L and wardrobe ₹1.1L",
            history = emptyList(),
            agentManager = agentManager
        )
        assertTrue(createReply.contains("rahul@omkun.com"))
        assertEquals(1, morphRepo.notesFlow.value.size)
        assertEquals("Interior Quote", morphRepo.notesFlow.value.first().title)

        // 3. Switch to Account B -> Rahul's notes must NOT leak into Priya's account
        morphRepo.disconnectAccount()
        assertTrue(morphRepo.notesFlow.value.isEmpty())

        val accountBResult = morphRepo.signInWithOmkunCredentials(
            email = "priya@omkun.com",
            password = "PriyaSecret456",
            displayName = "Priya Verma"
        )
        assertTrue(accountBResult.isSuccess)
        assertTrue("Account B should start with zero notes of its own", morphRepo.notesFlow.value.isEmpty())

        // Create a note in Priya's account
        morphRepo.createNoteFromAi(title = "Site Visit", content = "Check marble samples at 4 PM")
        assertEquals(1, morphRepo.notesFlow.value.size)
        assertEquals("Site Visit", morphRepo.notesFlow.value.first().title)

        // 4. Switch back to Account A -> Rahul's note is intact and isolated
        morphRepo.disconnectAccount()
        val reloginA = morphRepo.signInWithOmkunCredentials(
            email = "rahul@omkun.com",
            password = "StrongPassword123"
        )
        assertTrue(reloginA.isSuccess)
        agentManager.togglePlugin("morph_plugin", true)
        assertEquals(1, morphRepo.notesFlow.value.size)
        assertEquals("Interior Quote", morphRepo.notesFlow.value.first().title)

        // 5. Ask Oj AI to delete Rahul's note
        val deleteReply = OjAiService.generateResponse(
            userPrompt = "Delete Morph note Interior Quote",
            history = emptyList(),
            agentManager = agentManager
        )
        assertNotNull(deleteReply)
        assertTrue(morphRepo.notesFlow.value.isEmpty())
    }

    @Test
    fun `invalid credentials reject login and prevent dummy authentication`() = runBlocking {
        val result = morphRepo.signInWithOmkunCredentials(
            email = "rahul@omkun.com",
            password = "CompletelyWrongPassword!"
        )
        assertTrue("Invalid password must be rejected by production backend", result.isFailure)
        assertNull(morphRepo.connectedAccountFlow.value)
    }
}
