package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.RaniDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomChatDatabaseTest {

    private lateinit var db: RaniDatabase
    private lateinit var chatDao: ChatDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RaniDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        chatDao = db.chatDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadChatMessage() = runBlocking {
        val message = ChatMessage(
            id = "msg_1",
            role = MessageRole.ASSISTANT,
            text = "I am rani created by Sk Jahid Afridi. I am an autonomous ai how can I help you",
            source = "Rani Brain"
        )
        chatDao.insertMessage(ChatMessageEntity.fromDomain(message))

        val count = chatDao.getMessageCount()
        assertEquals(1, count)

        val retrieved = chatDao.getAllMessages().first()
        assertEquals(1, retrieved.size)
        assertEquals("msg_1", retrieved[0].id)
        assertEquals("I am rani created by Sk Jahid Afridi. I am an autonomous ai how can I help you", retrieved[0].text)
        assertEquals(MessageRole.ASSISTANT, retrieved[0].toDomain().role)
    }

    @Test
    fun updateMessageRatingInDb() = runBlocking {
        val message = ChatMessage(
            id = "msg_2",
            role = MessageRole.USER,
            text = "Explain SNN dynamics"
        )
        chatDao.insertMessage(ChatMessageEntity.fromDomain(message))

        chatDao.updateRating("msg_2", "good")
        val retrieved = chatDao.getAllMessages().first()
        assertEquals("good", retrieved[0].rating)
    }

    @Test
    fun replaceOldIntroSnippet() = runBlocking {
        val oldMessage = ChatMessage(
            id = "old_msg",
            role = MessageRole.ASSISTANT,
            text = "Hello! I am Rani v16 — your autonomous AI Operating System..."
        )
        chatDao.insertMessage(ChatMessageEntity.fromDomain(oldMessage))

        chatDao.updateOldIntroMessage(
            oldSnippet = "%Rani v16%",
            newText = "I am rani created by Sk Jahid Afridi. I am an autonomous ai how can I help you"
        )

        val retrieved = chatDao.getAllMessages().first()
        assertEquals("I am rani created by Sk Jahid Afridi. I am an autonomous ai how can I help you", retrieved[0].text)
    }
}
