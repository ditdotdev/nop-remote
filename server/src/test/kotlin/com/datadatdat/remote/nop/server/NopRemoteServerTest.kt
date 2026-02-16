/*
 * Copyright Datadatdat.
 */

package com.datadatdat.remote.nop.server

import com.datadatdat.remote.RemoteOperation
import com.datadatdat.remote.RemoteOperationType
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.clearAllMocks

class NopRemoteServerTest : StringSpec() {
    private val client = NopRemoteServer()

    private val op =
        RemoteOperation(
            operationId = "op",
            commitId = "commit",
            commit = null,
            remote = emptyMap(),
            parameters = emptyMap(),
            type = RemoteOperationType.PUSH,
            updateProgress = { _, _, _ -> },
        )

    override fun afterTest(
        testCase: TestCase,
        result: TestResult,
    ) {
        clearAllMocks()
    }

    init {

        "validate empy remote succeeds" {
            val result = client.validateRemote(emptyMap())
            result.size shouldBe 0
        }

        "validate remote with properties fails" {
            shouldThrow<IllegalArgumentException> {
                client.validateRemote(mapOf("a" to "b"))
            }
        }

        "validate null parameters succeeds" {
            val result = client.validateParameters(null)
            result.size shouldBe 0
        }

        "validate empty parameters succeeds" {
            val result = client.validateParameters(emptyMap())
            result.size shouldBe 0
        }

        "validate delay converts to integer" {
            val result = client.validateParameters((mapOf("delay" to 12.0)))
            result["delay"] shouldBe 12
        }

        "validate unknown properties fails" {
            shouldThrow<IllegalArgumentException> {
                client.validateParameters(mapOf("a" to "b"))
            }
        }

        "get provider returns nop" {
            client.getProvider() shouldBe "nop"
        }

        "get commit returns an empty commit" {
            val commit = client.getCommit(emptyMap(), emptyMap(), "id")
            commit shouldNotBe null
            commit!!.size shouldBe 0
        }

        "list commits returns an empty list" {
            val commits = client.listCommits(emptyMap(), emptyMap(), emptyList())
            commits.size shouldBe 0
        }

        "sync data end does nothing" {
            client.syncDataEnd(op, null, true)
        }

        "sync data volume does nothing" {
            client.syncDataVolume(op, null, "volume", "volume", "/path", "/path")
        }

        "sync data start succeeds" {
            client.syncDataStart(op)
        }

        "sync data start with delay zero skips sleep" {
            val opWithDelay =
                RemoteOperation(
                    operationId = "op",
                    commitId = "commit",
                    commit = null,
                    remote = emptyMap(),
                    parameters = mapOf("delay" to 0),
                    type = RemoteOperationType.PUSH,
                    updateProgress = { _, _, _ -> },
                )
            client.syncDataStart(opWithDelay)
        }

        "sync data start with delay sleeps" {
            val opWithDelay =
                RemoteOperation(
                    operationId = "op",
                    commitId = "commit",
                    commit = null,
                    remote = emptyMap(),
                    parameters = mapOf("delay" to 1),
                    type = RemoteOperationType.PUSH,
                    updateProgress = { _, _, _ -> },
                )
            val start = System.currentTimeMillis()
            client.syncDataStart(opWithDelay)
            val elapsed = System.currentTimeMillis() - start
            (elapsed >= 900) shouldBe true
        }

        "push metadata does nothing" {
            client.pushMetadata(op, emptyMap(), true)
        }
    }
}
