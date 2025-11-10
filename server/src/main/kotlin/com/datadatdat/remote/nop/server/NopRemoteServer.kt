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
     * Validate parameters, which are all optional (delay).
     */
    override fun validateParameters(parameters: Map<String, Any>?): Map<String, Any> {
        val params = parameters ?: emptyMap()
        util.validateFields(params, emptyList(), listOf("delay"))
        return params
    }    /**
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
