---
name: git-commit-workflow
description: Use when Codex needs to review Git changes, group related files into commits, write Conventional Commit messages, commit, push, inspect history, or prepare repository changes safely. Especially useful in this Carbon Android launcher repo when the user asks to commit, push, split commits by context, review staged files, or avoid committing local artifacts.
---

# Git Commit Workflow

Follow this workflow for Git work in the Carbon launcher repository.

## Inspect First

1. Run `git status --short` before staging anything.
2. Run focused diffs for modified files. Prefer `git diff -- <path>` and `git diff --stat` over broad output when the workspace is busy.
3. Identify generated, local, or unrelated files and leave them unstaged unless the user explicitly asks to include them.
4. Treat existing untracked `.agents/`, `skills-lock.json`, screenshots, emulator captures, and temporary images as local artifacts unless the user asks to version them.

## Group Commits

Use one commit when the changes form one coherent product increment. Split commits when changes are independently understandable, such as:

- UI behavior changes
- data or persistence changes
- documentation changes
- tooling, skills, or repo metadata
- tests or build configuration

When splitting, stage explicit paths. Avoid broad `git add .` in this repo unless the workspace was already verified clean.

## Validate

Before committing app code, run the smallest useful validation. For Carbon, prefer:

```bash
./gradlew.bat assembleDebug
```

If the user asked to install after changes, install the debug APK on the current device after a successful build.

## Commit Messages

Use Conventional Commits:

```text
type(scope): concise imperative summary
```

Common types:

- `feat`: new user-visible behavior
- `fix`: bug fix
- `refactor`: internal code change without behavior change
- `docs`: documentation only
- `chore`: repository maintenance or tooling
- `test`: tests only
- `build`: build/dependency changes

Keep the subject under about 72 characters when practical. Use a scope only when it adds clarity, for example `feat(dock): support scrollable pinned apps`.

## Push

After committing, verify the latest commit with `git log -1 --oneline`, then push the current branch when the user asked for push. Report the commit hash and branch.

## Safety Rules

- Never revert user changes unless explicitly requested.
- Never run destructive cleanup commands to make Git status pretty.
- Do not commit secrets, signing keys, local screenshots, emulator images, or personal tool caches unless explicitly requested.
- If a commit requires elevated Git access because `.git` is protected by the sandbox, request approval for the specific Git operation.
