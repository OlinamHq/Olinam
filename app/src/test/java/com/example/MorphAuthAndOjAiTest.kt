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

    @Test
    fun `short or empty morph api key is rejected before network call`() = runBlocking {
        val emptyRes = morphRepo.connectWithApiKey("")
        assertTrue(emptyRes.isFailure)

        val shortRes = morphRepo.connectWithApiKey("omk_123")
        assertTrue(shortRes.isFailure)
        assertNull(morphRepo.connectedAccountFlow.value)
    }

    @Test
    fun `read-only morph scopes block write and task creation`() = runBlocking {
        val readOnlyAccount = com.example.model.MorphAccount(
            accountId = "morph_key_readonly",
            email = "omk_live_••••9k1p",
            displayName = "Public Widget",
            authProvider = "API_KEY",
            apiKey = "omk_live_publ_test_9k1p",
            authorizedScopes = com.example.model.MorphAccount.READ_ONLY_SCOPES
        )
        assertTrue(readOnlyAccount.canReadDocs())
        assertTrue(readOnlyAccount.canReadDatabases())
        assertTrue(readOnlyAccount.canReadTasks())
        assertFalse(readOnlyAccount.canWriteDocs())
        assertFalse(readOnlyAccount.canWriteTasks())
        assertFalse(readOnlyAccount.hasFullAccess())
        assertEquals("omk_live_••••9k1p", readOnlyAccount.maskedApiKey())
    }

    @Test
    fun `1-tap olinam sso requires verified morph credentials when none saved`() = runBlocking {
        val unlinkedUser = com.example.model.UserProfile(
            id = "olinam_user_99",
            name = "Ankit",
            email = "unlinked_user_test@example.com",
            phoneNumber = "+919999999999"
        )
        val res = morphRepo.authorizeWithOlinamAccount(unlinkedUser)
        assertTrue(res.isFailure)
        assertEquals("MORPH_CREDENTIALS_REQUIRED", res.exceptionOrNull()?.message)
        assertNull(morphRepo.connectedAccountFlow.value)
    }

    @Test
    fun `morph rest parsers decode files tasks and databases schemas cleanly`() {
        val filesJson = """
            {
              "object": "list",
              "data": [
                {
                  "id": "doc-welcome",
                  "title": "Project Roadmap",
                  "favorite": true,
                  "status": "Published",
                  "tags": ["Planning", "Q4"],
                  "blocks": [
                    { "id": "b1", "type": "heading1", "content": "Overview" },
                    { "id": "b2", "type": "paragraph", "content": "Launch mobile sync." },
                    { "id": "b3", "type": "todo", "content": "Verify REST parser", "checked": true }
                  ]
                }
              ]
            }
        """.trimIndent()
        val parsedDocs = morphRepo.parseFilesResponse(filesJson, "acc_test")
        assertEquals(1, parsedDocs.size)
        val doc = parsedDocs.first()
        assertEquals("doc-welcome", doc.id)
        assertEquals("Project Roadmap", doc.title)
        assertTrue(doc.isPinned)
        assertTrue(doc.content.contains("# Overview"))
        assertTrue(doc.content.contains("Launch mobile sync."))
        assertEquals(1, doc.checklist.size)
        assertTrue(doc.checklist.first().isDone)
        assertEquals(listOf("Planning", "Q4"), doc.labels)

        val tasksJson = """
            {
              "object": "list",
              "data": [
                {
                  "id": "tsk-1",
                  "title": "Ship Android release",
                  "project": "Olinam",
                  "priority": "High",
                  "dueDate": "2026-12-01",
                  "status": "In Progress",
                  "favorite": true
                }
              ]
            }
        """.trimIndent()
        val parsedTasks = morphRepo.parseTasksResponse(tasksJson, "acc_test")
        assertEquals(1, parsedTasks.size)
        assertEquals("task_tsk-1", parsedTasks.first().id)
        assertTrue(parsedTasks.first().isPinned)
        assertTrue(parsedTasks.first().content.contains("Olinam"))
        assertTrue(parsedTasks.first().content.contains("High"))

        val dbsJson = """
            {
              "object": "list",
              "data": [
                {
                  "id": "db-roadmap",
                  "title": "Feature Tracker",
                  "description": "Release items",
                  "rows": [{ "id": "r1" }, { "id": "r2" }]
                }
              ]
            }
        """.trimIndent()
        val parsedDbs = morphRepo.parseDatabasesResponse(dbsJson)
        assertEquals(1, parsedDbs.size)
        assertEquals(2, parsedDbs.first().recordCount)
    }

    @Test
    fun `firestore document parser handles doubleValue timestamps nested arrays and favorite flag`() {
        val firestoreDoc = org.json.JSONObject(
            """
            {
              "name": "projects/morph-69d82/databases/(default)/documents/documents/page-123",
              "fields": {
                "title": { "stringValue": "Design Spec" },
                "favorite": { "booleanValue": true },
                "updatedAt": { "doubleValue": 1732100000000.0 },
                "blocks": {
                  "mapValue": {
                    "fields": {
                      "_firestoreNestedArr": { "booleanValue": true },
                      "values": {
                        "arrayValue": {
                          "values": [
                            {
                              "mapValue": {
                                "fields": {
                                  "id": { "stringValue": "blk-1" },
                                  "type": { "stringValue": "heading2" },
                                  "content": { "stringValue": "Architecture" }
                                }
                              }
                            },
                            {
                              "mapValue": {
                                "fields": {
                                  "id": { "stringValue": "blk-2" },
                                  "type": { "stringValue": "todo" },
                                  "content": { "stringValue": "Review schema" },
                                  "checked": { "booleanValue": false }
                                }
                              }
                            }
                          ]
                        }
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()
        )
        val parsed = morphRepo.parseFirestoreDocumentToMorphNote(
            docObj = firestoreDoc,
            accountId = "uid_123",
            defaultSourceType = com.example.model.MorphNote.SOURCE_DOC
        )
        assertNotNull(parsed)
        assertEquals("page-123", parsed!!.id)
        assertEquals("Design Spec", parsed.title)
        assertTrue(parsed.isPinned)
        assertEquals(1732100000000L, parsed.updatedAt)
        assertTrue(parsed.content.contains("## Architecture"))
        assertEquals(1, parsed.checklist.size)
        assertEquals("Review schema", parsed.checklist.first().text)
    }
}
