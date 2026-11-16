# Extension policy

Extensions must declare identity, version, publisher, license, requested permissions and supported API version. The host must validate the manifest, grant permissions explicitly, and provide uninstall/revocation controls.

Do not execute arbitrary downloaded extension code in the assistant process. Prefer declarative capabilities and narrowly scoped IPC/API contracts.
