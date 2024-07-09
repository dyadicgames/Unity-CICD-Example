# Use the TeamCity agent image as the base
FROM jetbrains/teamcity-agent:latest

# Use the Unity image as the base for the final image
FROM unityci/editor:2022.3.31f1-windows-mono-3.1.0

# Mount the secret credentials
RUN --mount=type=secret,id=unity_user,target=/run/secrets/unity_user \
    --mount=type=secret,id=unity_pass,target=/run/secrets/unity_pass \
    --mount=type=secret,id=unity_serial,target=/run/secrets/unity_serial \
    /opt/unity/Editor/Unity -batchmode -quit \
    -serial $(cat /run/secrets/unity_serial) \
    -username $(cat /run/secrets/unity_user) \
    -password $(cat /run/secrets/unity_pass)

# Copy the TeamCity agent files from the first stage
COPY --from=0 /opt/buildagent /opt/buildagent
COPY --from=0 /opt/java /opt/java

# Set environment variables for TeamCity
ENV JAVA_HOME=/opt/java/openjdk
ENV FJ_DEBUG=1

# Set the entrypoint to the TeamCity agent
ENTRYPOINT ["/opt/buildagent/bin/agent.sh", "run"]