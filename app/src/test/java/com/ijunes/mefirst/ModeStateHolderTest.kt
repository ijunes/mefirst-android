package com.ijunes.mefirst

import android.content.Context
import android.content.SharedPreferences
import com.ijunes.mefirst.common.state.ModeStateHolder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ModeStateHolderTest {

    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockContext: Context
    private lateinit var holder: ModeStateHolder

    @Before
    fun setUp() {
        mockEditor = mockk(relaxed = true)
        mockPrefs = mockk {
            every { getBoolean("is_work_mode", false) } returns false
            every { edit() } returns mockEditor
        }
        mockContext = mockk {
            every { getSharedPreferences("app_prefs", Context.MODE_PRIVATE) } returns mockPrefs
        }
        holder = ModeStateHolder(mockContext)
    }

    @Test
    fun `initial mode is personal (false) when no preference stored`() {
        assertFalse(holder.isWorkMode.value)
    }

    @Test
    fun `setWorkMode true updates StateFlow to true`() {
        holder.setWorkMode(true)
        assertTrue(holder.isWorkMode.value)
    }

    @Test
    fun `setWorkMode persists value to SharedPreferences`() {
        holder.setWorkMode(true)
        verify { mockEditor.putBoolean("is_work_mode", true) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `initial mode is work when SharedPreferences returns true`() {
        every { mockPrefs.getBoolean("is_work_mode", false) } returns true
        val holderWithWorkMode = ModeStateHolder(mockContext)
        assertTrue(holderWithWorkMode.isWorkMode.value)
    }
}
