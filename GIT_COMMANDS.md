# Git Commands Reference

This document provides a step-by-step guide for Git operations from repository initialization to collaboration workflows.

## 1. Initialize Git Repository

```bash
# Initialize a new Git repository
git init

# Set default branch to main (if not already configured globally)
git config --global init.defaultBranch main

# Or rename current branch to main if already initialized
git branch -M main
```

## 2. Initial Configuration (First Time Setup)

```bash
# Set your identity
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Optional: Set default editor
git config --global core.editor "code --wait"  # For VS Code
```

## 3. Add Files and Make Initial Commit

```bash
# Check repository status
git status

# Add all files to staging area
git add .

# Or add specific files
git add filename.txt

# Make your first commit
git commit -m "Initial commit"
```

## 4. Connect to Remote Repository (Origin)

```bash
# Add remote origin (replace URL with your repository URL)
git remote add origin https://github.com/username/repository-name.git

# Or for SSH
git remote add origin git@github.com:username/repository-name.git

# Verify remote connection
git remote -v
```

## 5. Push to Remote Repository

```bash
# Push and set upstream for the first time
git push -u origin main

# For subsequent pushes (after upstream is set)
git push
```

## 6. Pull Changes from Remote

```bash
# Pull latest changes from remote
git pull origin main

# Or simply (if upstream is set)
git pull

# Pull with rebase (alternative)
git pull --rebase origin main
```

## Complete Workflow Order

### First Time Setup (New Repository):
1. `git init`
2. `git config --global init.defaultBranch main` (if needed)
3. `git add .`
4. `git commit -m "Initial commit"`
5. `git remote add origin <repository-url>`
6. `git push -u origin main`

### Daily Workflow:
1. `git pull` (get latest changes)
2. Make your changes
3. `git add .` (stage changes)
4. `git commit -m "Your commit message"`
5. `git push` (push to remote)

## Common Git Commands

### Repository Status and History
```bash
# Check status of working directory
git status

# View commit history
git log --oneline

# View changes in files
git diff
```

### Branch Operations
```bash
# List all branches
git branch

# Create new branch
git checkout -b feature-branch-name

# Switch to existing branch
git checkout branch-name

# Merge branch into current branch
git merge branch-name

# Delete branch
git branch -d branch-name
```

### Undoing Changes
```bash
# Unstage files
git reset HEAD filename

# Discard changes in working directory
git checkout -- filename

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Undo last commit (discard changes)
git reset --hard HEAD~1
```

### Remote Operations
```bash
# List remotes
git remote -v

# Add remote
git remote add upstream <url>

# Remove remote
git remote remove origin

# Fetch changes without merging
git fetch origin
```

## Best Practices

1. **Always pull before pushing**: `git pull` before `git push`
2. **Use meaningful commit messages**: Be descriptive about what changed
3. **Commit frequently**: Small, logical commits are better than large ones
4. **Use branches**: Create feature branches for new development
5. **Review changes**: Use `git diff` to review changes before committing

## Troubleshooting

### Common Issues and Solutions

**Issue**: "fatal: remote origin already exists"
```bash
# Solution: Remove existing remote and add new one
git remote remove origin
git remote add origin <new-url>
```

**Issue**: Merge conflicts
```bash
# Solution: Resolve conflicts manually, then:
git add .
git commit -m "Resolve merge conflicts"
```

**Issue**: Push rejected (non-fast-forward)
```bash
# Solution: Pull first, then push
git pull origin main
git push origin main
```

## Quick Reference

| Command | Description |
|---------|-------------|
| `git init` | Initialize repository |
| `git add .` | Stage all changes |
| `git commit -m "message"` | Commit with message |
| `git push` | Push to remote |
| `git pull` | Pull from remote |
| `git status` | Check repository status |
| `git log` | View commit history |
| `git branch` | List branches |
| `git checkout -b name` | Create and switch to new branch |
| `git merge branch` | Merge branch into current |

---

*Last updated: July 19, 2025*
