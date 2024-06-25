/**
 * Provides utilities and classes for serializing Java objects to JSON and deserializing JSON
 * back to Java objects using Gson library.
 * <p>
 * This package includes:
 * - {@link edu.university.ecs.lab.common.models.serialization.JsonSerializable}: Interface for classes
 *   that can be serialized to JSON objects.
 * - {@link edu.university.ecs.lab.common.models.serialization.MethodCallDeserializer}: Deserializer
 *   for converting JSON to {@link edu.university.ecs.lab.common.models.ir.MethodCall} and
 *   {@link edu.university.ecs.lab.common.models.ir.RestCall} objects.
 * - {@link edu.university.ecs.lab.common.models.serialization.MethodDeserializer}: Deserializer for
 *   converting JSON to {@link edu.university.ecs.lab.common.models.ir.Method} and
 *   {@link edu.university.ecs.lab.common.models.ir.Endpoint} objects.
 * <p>
 * These classes facilitate conversion between Java objects and JSON representations.
 */
package edu.university.ecs.lab.common.models.serialization;
