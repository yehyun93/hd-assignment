#!/bin/bash

BASE_URL="http://localhost:8080"
USERS_PER_AGE=20  # 연령대별 20명씩 = 총 160명

echo "시작: Rate Limiter 테스트용 사용자 생성 (연령대별 ${USERS_PER_AGE}명)"

# 주민등록번호 생성 함수
generate_resident_number() {
    local birth_year=$1  # 4자리 연도 (예: 1990, 2005)
    local month=$(printf "%02d" $((RANDOM%12+1)))
    local day=$(printf "%02d" $((RANDOM%28+1)))
    
    # 연도의 마지막 2자리
    local yy=$(echo $birth_year | cut -c3-4)
    
    # 성별 코드 결정
    local gender_code
    if [ $birth_year -lt 2000 ]; then
        gender_code=$((1 + RANDOM % 2))  # 1900년대: 1(남) 또는 2(여)
    else
        gender_code=$((3 + RANDOM % 2))  # 2000년대: 3(남) 또는 4(여)
    fi
    
    # 뒷자리 6자리 (성별코드 1자리 + 나머지 5자리)
    # 총 13자리: YY(2) + MM(2) + DD(2) + 성별코드(1) + 나머지(6) = 13자리
    local suffix=$(printf "%06d" $((RANDOM%1000000)))
    
    echo "${yy}${month}${day}${gender_code}${suffix}"
}

create_users() {
    local age_group=$1
    local birth_year_start=$2
    local birth_year_end=$3
    local success=0
    
    echo "${age_group} 사용자 생성 시작..."
    
    for i in $(seq 1 $USERS_PER_AGE); do
        # 해당 연령대 범위에서 랜덤 생년 선택
        local birth_year=$((birth_year_start + RANDOM % (birth_year_end - birth_year_start + 1)))
        local resident_number=$(generate_resident_number $birth_year)
        
        user_id="${age_group}$(printf "%03d" $i)"
        name="테스트$(printf "%03d" $i)"
        phone="010$(printf "%08d" $((RANDOM%100000000)))"
        
        response=$(curl -s -w "%{http_code}" -o /dev/null \
            -X POST "$BASE_URL/users/register" \
            -H "Content-Type: application/json" \
            -d "{
                \"userId\": \"$user_id\",
                \"password\": \"pass123!\",
                \"name\": \"$name\",
                \"residentNumber\": \"$resident_number\",
                \"phoneNumber\": \"$phone\",
                \"address\": \"서울특별시 강남구\"
            }")
        
        if [ "$response" = "200" ]; then
            success=$((success + 1))
        else
            # 실패 시 첫 5개만 디버깅 출력
            if [ $i -le 5 ]; then
                echo "  실패: $user_id (HTTP: $response) - 주민번호: $resident_number"
            fi
        fi
        
        # 진행률 표시 (5개마다)
        if [ $((i % 5)) -eq 0 ]; then
            echo "  진행: $i/$USERS_PER_AGE (성공: $success)"
        fi
        
        sleep 0.01
    done
    
    echo "${age_group} 완료: ${success}/${USERS_PER_AGE}명 성공"
}

# 2025년 기준 연령대별 생년 범위
echo "📅 2025년 기준 연령대별 사용자 생성..."

create_users "teens" 2006 2015      # 10-19세
create_users "twenties" 1996 2005   # 20-29세  
create_users "thirties" 1986 1995   # 30-39세
create_users "forties" 1976 1985    # 40-49세
create_users "fifties" 1966 1975    # 50-59세
create_users "sixties" 1956 1965    # 60-69세
create_users "seventies" 1946 1955  # 70-79세
create_users "over80" 1930 1945     # 80세 이상

echo ""
echo "종료: 총 생성 완료"
echo ""