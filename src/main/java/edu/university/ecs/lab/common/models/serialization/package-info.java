/**
 * Provides utilities and classes for serializing Java objects to JSON and deserializing JSON
 * back to Java objects using Gson library.
 * <p>
 * This package includes:
 * - {@link edu.university.ecs.lab.common.models.serialization.JsonSerializable}: Interface for classes
 *   that can be serialized to JSON objects.
 * - {@link edu.university.ecs.lab.common.models.serialization.MethodCallDeserializer}: Deserializer
 *   for converting JSON to {@link edu.university.ecs.lab.common.models.MethodCall} and
 *   {@link edu.university.ecs.lab.common.models.RestCall} objects.
 * - {@link edu.university.ecs.lab.common.models.serialization.MethodDeserializer}: Deserializer for
 *   converting JSON to {@link edu.university.ecs.lab.common.models.Method} and
 *   {@link edu.university.ecs.lab.common.models.Endpoint} objects.
 * <p>
 * These classes facilitate conversion between Java objects and JSON representations.
 */
package edu.university.ecs.lab.common.models.serialization;
