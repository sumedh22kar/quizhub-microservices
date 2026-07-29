$quizServiceUrl = "http://localhost:9002/api/v1/quizzes"
$questionServiceUrl = "http://localhost:9003/api/v1/questions"

Write-Host "==================================================="
Write-Host "1. Create a real Quiz in quiz-service"
Write-Host "==================================================="
$createQuizBody = @{
    title = "Java Spring Boot Quiz"
    description = "Integration test quiz"
    difficulty = "MEDIUM"
    status = "DRAFT"
    durationMinutes = 30
    totalMarks = 50
    ownerId = "11111111-2222-3333-4444-555555555555"
} | ConvertTo-Json

$quizRes = Invoke-RestMethod -Uri $quizServiceUrl -Method Post -Body $createQuizBody -ContentType "application/json"
$existingQuizId = $quizRes.data.id
Write-Host ("Existing Quiz Created with ID: " + $existingQuizId)
Write-Host ""

Write-Host "==================================================="
Write-Host "2. Create Question with EXISTING Quiz ID (POST /api/v1/questions)"
Write-Host "==================================================="
$createQuestionBody1 = @{
    quizId = $existingQuizId
    questionText = "What is Spring Boot?"
    questionType = "MCQ"
    optionA = "Framework"
    optionB = "Database"
    optionC = "Browser"
    optionD = "OS"
    correctAnswer = "Framework"
    marks = 5
} | ConvertTo-Json

$qRes1 = Invoke-RestMethod -Uri $questionServiceUrl -Method Post -Body $createQuestionBody1 -ContentType "application/json"
$qRes1 | ConvertTo-Json -Depth 5
Write-Host "EXPECTED: 201 Created / Success = true"
Write-Host ""

Write-Host "==================================================="
Write-Host "3. Create Question with FAKE / RANDOM Quiz ID (POST /api/v1/questions)"
Write-Host "==================================================="
$fakeQuizId = [System.Guid]::NewGuid().ToString()
Write-Host ("Using Fake Quiz ID: " + $fakeQuizId)

$createQuestionBody2 = @{
    quizId = $fakeQuizId
    questionText = "Will this fail?"
    questionType = "MCQ"
    optionA = "Yes"
    optionB = "No"
    correctAnswer = "Yes"
    marks = 5
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri $questionServiceUrl -Method Post -Body $createQuestionBody2 -ContentType "application/json"
} catch {
    Write-Host "EXPECTED 404 NOT FOUND EXCEPTION CAUGHT:"
    $streamReader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $errorResponseBody = $streamReader.ReadToEnd()
    Write-Host $errorResponseBody
}
