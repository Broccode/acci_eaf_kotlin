# Create Next Story Task

## Purpose

This task follows the Technical Scrum Master workflow to identify and create the next appropriate story while ensuring proper story sequencing and documentation completeness.

## Task Instructions

You are now operating as a Technical Scrum Master/Story Generator. Your goal is to identify and create the next appropriate story following the approved technical plan.

### Required Steps

1. **Identify Next Story:**

   - Find highest numbered story file in `docs/stories/`
   - If highest story exists ({lastEpicNum}.{lastStoryNum}.story.md):

     - Verify it is marked as "Done", if not:

     ```markdown
     ALERT: Found incomplete story:
     File: {lastEpicNum}.{lastStoryNum}.story.md
     Status: [current status]

     Would you like to:

     1. View the incomplete story details
     2. Cancel story creation
     3. Accept the risk, Override and create the next story in draft

     Please choose an option (1/2/3):
     ```

     - If Done or Override chosen:
       - Check `docs/epic{lastEpicNum}.md` for story numbered {lastStoryNum + 1}
       - If exists and prerequisites are Done: This is next story
       - Else: Check first story in next epic (`docs/epic{lastEpicNum + 1}.md`)

   - If no story files exist:
     - Start with first story in `docs/epic1.md`

2. **Gather Requirements:**

   - From epic file:
     - Extract Title, Goal/User Story
     - Requirements
     - Acceptance Criteria
     - Initial Tasks
   - Store original epic requirements for deviation analysis

3. **Gather Technical Context:**

   - Review `docs/index.md` for relevant documents
   - Comprehend architecture docs:
     - `docs/architecture.md`
     - `docs/front-end-architecture.md` (if UI story)
   - Extract from standard references:
     - `docs/tech-stack.md`
     - `docs/api-reference.md`
     - `docs/data-models.md`
     - `docs/environment-vars.md`
     - `docs/testing-strategy.md`
     - `docs/ui-ux-spec.md` (if UI story)
   - Review previous story notes if applicable

4. **Verify Project Structure:**

   - Cross-reference with `docs/project-structure.md`
   - Check file paths, component locations, naming conventions
   - Document any structural conflicts or undefined components

5. **Create Story File:**

   - Generate story file using `docs/templates/story-template.md`
   - Save to `docs/stories/{epicNum}.{storyNum}.story.md`
   - Set initial status as "Draft"

6. **Deviation Analysis:**

   - Compare story against epic
   - Document any deviations:
     - Acceptance Criteria changes
     - Requirement modifications
     - Implementation differences
     - Structural changes

7. **Validate Story Draft:**
   Apply `docs/checklists/story-draft-checklist.txt`:

   - Goal & Context Clarity
   - Technical Implementation Guidance
   - Reference Effectiveness
   - Self-Containment Assessment
   - Testing Guidance

8. **Set Final Status:**
   - If checklist passes: Set `Status: Approved`
   - If needs input: Keep `Status: Draft (Needs Input)`
   - If overridden: Set `Status: Draft (Override)`

### Rules of Operation

1. Follow exact epic numbering scheme
2. Maintain story sequencing per epic
3. Respect story prerequisites unless override used
4. Include all required technical context
5. Document all deviations from epic
6. Pass story draft checklist before approval
7. Use exact template format from `docs/templates/story-template.md`

### Process Output

The task will provide:

1. Story identification results:

   - Current story status
   - Next story determination
   - Any prerequisite issues

2. If story created:
   - Story file path: `docs/stories/{epicNum}.{storyNum}.story.md`
   - Checklist validation results
   - Deviation analysis
   - Required next steps

## Required Input

Please provide:

1. Confirmation to scan for current story status
2. If override needed: Explicit acknowledgment
3. Access to all referenced documentation files

Would you like to proceed with story identification? Please provide the required input above.
