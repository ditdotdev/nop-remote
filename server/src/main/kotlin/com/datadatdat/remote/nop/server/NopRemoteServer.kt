/*
 * Copyright Datadatdat.
 */

package com.datadatdat.remote.nop.server

import com.datadatdat.remote.RemoteOperation
import com.datadatdat.remote.RemoteServer

/**
 * The nop (No-operation) is a special provider used for internal testing to make it easier to
 * test local workflows without having to mock out an external remote provider. As its name implies,
 * this will simply ignore any operations. Pushing and pulling will always succeed, though listing
 * remotes will always return an empty list.
 */
class NopRemoteServer : RemoteServer {
    override fun getProvider(): String {
        return "nop"
    }

    /**
     * Nop remotes are not allowed to have any configuration.
     */
    override fun validateRemote(remote: Map<String, Any>): Map<String, Any> {
        if (remote.size != 0) {
            throw IllegalArgumentException("invalid nop remote property '${remote.keys.first()}")
        }
        return remote
    }

    /**
     * The only nop parameter supported is "delay". But since that is passed as an integer, we need to conver it
     * from a double (default number type).
     */
    override fun validateParameters(parameters: Map<String, Any>): Map<String, Any> {
        for (prop in parameters.keys) {
            if (prop != "delay") {
                throw java.lang.IllegalArgumentException("invalid nop remote parameter '$prop'")
            }
        }

        return if (parameters.containsKey("delay")) {
            mapOf("delay" to (parameters["delay"] as Double).toInt())
        } else {
            emptyMap()
        }
    }

    /**
     * The nop provider always returns success for any commit, and returns an empty set of properties.
     */
    override fun getCommit(
        remote: Map<String, Any>,
        parameters: Map<String, Any>,
        commitId: String,
    ): Map<String, Any>? {
        return emptyMap()
    }

    /**
     * The nop provider always returns an empty list of commits.
     */
    override fun listCommits(
        remote: Map<String, Any>,
        parameters: Map<String, Any>,
        tags: List<Pair<String, String?>>,
    ): List<Pair<String, Map<String, Any>>> {
        return emptyList()
    }

    /**
     * There is nothing to do for nop operations, but we enable the ability to inject a delay into the process for the
     * purposes of controlling timing for tests.
     */
    override fun syncDataStart(operation: RemoteOperation) {
        val props = operation.parameters
        if (props.containsKey("delay")) {
            val delay = props.get("delay").toString().toDouble().toInt()
            if (delay != 0) {
                Thread.sleep(delay * 1000L)
            }
        }
    }

    override fun syncDataEnd(
        operation: RemoteOperation,
        operationData: Any?,
        isSuccessful: Boolean,
    ) {
        // Nothing to do
    }

    override fun syncDataVolume(
        operation: RemoteOperation,
        operationData: Any?,
        volumeName: String,
        volumeDescription: String,
        volumePath: String,
        scratchPath: String,
    ) {
        // Nothing to do
    }

    override fun pushMetadata(
        operation: RemoteOperation,
        commit: Map<String, Any>,
        isUpdate: Boolean,
    ) {
        // Nothing to do
    }
}
