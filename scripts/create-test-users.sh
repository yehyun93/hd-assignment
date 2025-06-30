#!/bin/bash

# 다양한 연령대의 테스트 사용자 생성 스크립트

echo "테스트 사용자 생성 시작..."

# API 기본 URL
BASE_URL="http://localhost:8080"

# 색상 코드
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 회원가입 함수
register_user() {
    local userId=$1
    local name=$2
    local residentNumber=$3
    local phoneNumber=$4
    local address=$5
    
    echo -n "사용자 등록 중: $name ($userId)... "
    
    response=$(curl -s -X POST "$BASE_URL/users/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"userId\": \"$userId\",
            \"password\": \"password123\",
            \"name\": \"$name\",
            \"residentNumber\": \"$residentNumber\",
            \"phoneNumber\": \"$phoneNumber\",
            \"address\": \"$address\"
        }" \
        -w "\n%{http_code}")
    
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        echo -e "${GREEN}성공${NC}"
    else
        echo -e "${RED}실패 (HTTP $http_code)${NC}"
    fi
}

# 테스트 사용자 데이터
# 형식: userId, 이름, 주민번호(13자리), 전화번호(11자리), 주소

echo ""
echo "10대 사용자 생성..."
register_user "teen1" "김민준" "0801011234567" "01012341001" "서울특별시 강남구 역삼동"
register_user "teen2" "이서연" "0905024234567" "01012341002" "경기도 성남시 분당구"

echo ""
echo "20대 사용자 생성..."
register_user "twenty1" "박지훈" "9801013234567" "01012342001" "부산광역시 해운대구"
register_user "twenty2" "최유나" "9905024234567" "01012342002" "대구광역시 수성구"
register_user "twenty3" "정민수" "0001013234567" "01012342003" "인천광역시 남동구"

echo ""
echo "30대 사용자 생성..."
register_user "thirty1" "강동현" "8801011234567" "01012343001" "광주광역시 서구"
register_user "thirty2" "윤서영" "9005022234567" "01012343002" "대전광역시 유성구"
register_user "thirty3" "임재현" "9201011234567" "01012343003" "울산광역시 남구"

echo ""
echo "40대 사용자 생성..."
register_user "forty1" "황준호" "7801011234567" "01012344001" "세종특별자치시 조치원읍"
register_user "forty2" "송미경" "8205022234567" "01012344002" "경기도 수원시 영통구"

echo ""
echo "50대 사용자 생성..."
register_user "fifty1" "김철수" "6801011234567" "01012345001" "강원특별자치도 춘천시"
register_user "fifty2" "이영희" "7205022234567" "01012345002" "충청북도 청주시"

echo ""
echo "60대 사용자 생성..."
register_user "sixty1" "박상철" "5801011234567" "01012346001" "충청남도 천안시"
register_user "sixty2" "정순희" "6205022234567" "01012346002" "전북특별자치도 전주시"

echo ""
echo "70대 사용자 생성..."
register_user "seventy1" "최기남" "4801011234567" "01012347001" "전라남도 목포시"
register_user "seventy2" "김정숙" "5205022234567" "01012347002" "경상북도 포항시"

echo ""
echo "80대 이상 사용자 생성..."
register_user "eighty1" "이만수" "3801011234567" "01012348001" "경상남도 창원시"
register_user "eighty2" "박순자" "4205022234567" "01012348002" "제주특별자치도 제주시"

echo ""
echo "테스트 사용자 생성 완료!"
echo ""
echo "관리자 계정으로 사용자 목록 확인:"
curl -s -X GET "$BASE_URL/admin/users" -u admin:1212 | jq '.'
