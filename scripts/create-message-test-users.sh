#!/bin/bash

# 30대 회원 100명 생성 스크립트
# 사용법: ./create-30s-users.sh

BASE_URL="http://localhost:8080"
TOTAL_USERS=100

echo "🚀 30대 회원 ${TOTAL_USERS}명 생성 시작..."
echo "📍 서버 URL: ${BASE_URL}"
echo "============================================"

# 30대를 위한 생년월일 범위 (1985-1994년생)
# 2025년 기준으로 30-39세

# 성공/실패 카운터
SUCCESS_COUNT=0
FAILURE_COUNT=0

# 진행률 표시 함수
show_progress() {
    local current=$1
    local total=$2
    local percent=$((current * 100 / total))
    printf "\r📊 진행률: [%d/%d] %d%% (성공: %d, 실패: %d)" "$current" "$total" "$percent" "$SUCCESS_COUNT" "$FAILURE_COUNT"
}

# 30대 회원 생성 함수
create_30s_user() {
    local index=$1
    
    # 30대 생년월일 생성 (1985-1994년생)
    local birth_year=$((1985 + RANDOM % 10))  # 1985~1994
    local birth_month=$(printf "%02d" $((1 + RANDOM % 12)))  # 01~12
    local birth_day=$(printf "%02d" $((1 + RANDOM % 28)))    # 01~28 (안전하게)
    
    # 성별 코드 (1,2: 1900년대, 3,4: 2000년대)
    local gender_code=$((1 + RANDOM % 2))  # 1 또는 2 (1900년대)
    
    # 주민등록번호 생성 (13자리 숫자만)
    local birth_date="${birth_year#19}${birth_month}${birth_day}"  # YYMMDD
    local resident_number="${birth_date}${gender_code}$(printf "%06d" $((RANDOM % 1000000)))"
    
    # 사용자 정보 생성
    local user_id="user30_$(printf "%03d" "$index")"
    local password="password123!"  # 8자 이상으로 변경
    local name="김삼십$(printf "%03d" "$index")"
    local phone="010$(printf "%04d" $((3000 + index)))$(printf "%04d" $((RANDOM % 10000)))"  # 11자리 숫자만
    local address="서울특별시 강남구 테헤란로 $(printf "%d" $((100 + index)))길"
    
    # JSON 데이터 생성
    local json_data=$(cat <<EOF
{
    "userId": "${user_id}",
    "password": "${password}",
    "name": "${name}",
    "residentNumber": "${resident_number}",
    "phoneNumber": "${phone}",
    "address": "${address}"
}
EOF
)
    
    # API 호출
    local response=$(curl -s -w "%{http_code}" -o /tmp/response_${index}.json \
        -X POST "${BASE_URL}/users/register" \
        -H "Content-Type: application/json" \
        -d "$json_data")
    
    local http_code="${response: -3}"
    
    if [ "$http_code" = "200" ]; then
        ((SUCCESS_COUNT++))
        # 성공 시 상세 정보는 verbose 모드에서만 출력
        if [ "$VERBOSE" = "true" ]; then
            echo ""
            echo "✅ 사용자 생성 성공: ${user_id} (${name}, ${resident_number})"
        fi
    else
        ((FAILURE_COUNT++))
        local error_msg=$(cat /tmp/response_${index}.json 2>/dev/null || echo "Unknown error")
        if [ "$VERBOSE" = "true" ]; then
            echo ""
            echo "❌ 사용자 생성 실패: ${user_id} (HTTP: ${http_code}) - ${error_msg}"
        fi
    fi
    
    # 임시 파일 정리
    rm -f /tmp/response_${index}.json
}

# 메인 실행
echo "⏳ 30대 회원 생성 중..."

for i in $(seq 1 $TOTAL_USERS); do
    create_30s_user "$i"
    show_progress "$i" "$TOTAL_USERS"
    
    # API 부하 방지를 위한 짧은 딜레이
    sleep 0.1
done

echo ""
echo "============================================"
echo "🎉 30대 회원 생성 완료!"
echo "📊 결과: 성공 ${SUCCESS_COUNT}명, 실패 ${FAILURE_COUNT}명"
echo "📈 성공률: $((SUCCESS_COUNT * 100 / TOTAL_USERS))%"

# 생성된 30대 회원 확인
echo ""
echo "🔍 생성된 30대 회원 확인 중..."
curl -s -X GET "${BASE_URL}/demo/test-users" | jq '.[] | select(.ageGroup == "30대") | {name, ageGroup, phone}' 2>/dev/null || echo "jq가 설치되지 않아 JSON 파싱을 건너뜁니다."

echo ""
echo "✅ 스크립트 실행 완료!"
echo "💡 메시지 발송 테스트: curl -X POST '${BASE_URL}/admin/messages/send-by-age-group' -H 'Content-Type: application/json' -d '{\"ageGroup\":\"30대\",\"customMessage\":\"테스트 메시지\"}'"