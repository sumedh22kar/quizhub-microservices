$baseUrl = "http://localhost:9003/api/v1/questions"
$quizId = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"

Write-Host "==================================================="
Write-Host "1. POST /api/v1/questions (Create Question)"
Write-Host "==================================================="
$createBody = @{
    quizId = $quizId
    questionText = "What is Java?"
    questionType = "MCQ"
    optionA = "Language"
    optionB = "Database"
    optionC = "Browser"
    optionD = "OS"
    correctAnswer = "Language"
    marks = 5
} | ConvertTo-Json

$res1 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $createBody -ContentType "application/json"
$res1 | ConvertTo-Json -Depth 5
$questionId = $res1.data.id
Write-Host ("Created Question ID: " + $questionId)
Write-Host ""

Write-Host "==================================================="
Write-Host "2. GET /api/v1/questions/{id} (Get Question)"
Write-Host "==================================================="
$res2 = Invoke-RestMethod -Uri "$baseUrl/$questionId" -Method Get
$res2 | ConvertTo-Json -Depth 5
Write-Host ""

Write-Host "==================================================="
Write-Host "3. GET /api/v1/questions/quiz/{quizId} (Get Questions of Quiz)"
Write-Host "==================================================="
$res3 = Invoke-RestMethod -Uri "$baseUrl/quiz/$quizId" -Method Get
$res3 | ConvertTo-Json -Depth 5
Write-Host ""

Write-Host "==================================================="
Write-Host "4. PUT /api/v1/questions/{id} (Update Question)"
Write-Host "==================================================="
$updateBody = @{
    quizId = $quizId
    questionText = "What is Java 21?"
    questionType = "MCQ"
    optionA = "Programming Language & Runtime"
    optionB = "Database"
    optionC = "Browser"
    optionD = "Operating System"
    correctAnswer = "Programming Language & Runtime"
    marks = 10
} | ConvertTo-Json

$res4 = Invoke-RestMethod -Uri "$baseUrl/$questionId" -Method Put -Body $updateBody -ContentType "application/json"
$res4 | ConvertTo-Json -Depth 5
Write-Host ""

Write-Host "==================================================="
Write-Host "5. PATCH /api/v1/questions/{id}/deactivate (Deactivate Question)"
Write-Host "==================================================="
$res5 = Invoke-RestMethod -Uri "$baseUrl/$questionId/deactivate" -Method Patch
$res5 | ConvertTo-Json -Depth 5
Write-Host ""

Write-Host "==================================================="
Write-Host "6. PATCH /api/v1/questions/{id}/activate (Activate Question)"
Write-Host "==================================================="
$res6 = Invoke-RestMethod -Uri "$baseUrl/$questionId/activate" -Method Patch
$res6 | ConvertTo-Json -Depth 5
Write-Host ""

Write-Host "==================================================="
Write-Host "7. DELETE /api/v1/questions/{id} (Delete Question)"
Write-Host "==================================================="
$res7 = Invoke-RestMethod -Uri "$baseUrl/$questionId" -Method Delete
$res7 | ConvertTo-Json -Depth 5
Write-Host ""

Write-Host "==================================================="
Write-Host "8. Verification: GET /api/v1/questions/{id} after deletion"
Write-Host "==================================================="
try {
    Invoke-RestMethod -Uri "$baseUrl/$questionId" -Method Get
} catch {
    Write-Host ("Expected 404 Exception received: " + $_.Exception.Message)
}
