#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
펀드 페이지에서 '기준가'를 크롤링해서
터미널(console)에 바로 출력하는 간단 버전
CLI 인자를 받아서 동적으로 크롤링 가능
"""

import requests
from bs4 import BeautifulSoup
import re
import sys
from decimal import Decimal


def extract_current_price(html: str):
    """
    HTML에서 기준가를 추출
    """
    soup = BeautifulSoup(html, "html.parser")

    # '기준가' 라벨이 있는 요소 탐색
    item = None
    for it in soup.select(".boxs .space .item"):
        name = it.select_one(".name")
        if name and "기준가" in name.get_text(strip=True):
            item = it
            break

    if item:
        num = item.select_one(".num")
        if num:
            for em in num.select("em"):
                em.extract()
            raw = num.get_text(strip=True)
            m = re.search(r"[\d,]+(?:\.\d+)?", raw)
            if m:
                return Decimal(m.group(0).replace(",", ""))
    return None


def crawl_fund_price(fund_cd: str, child_fund_cd: str) -> Decimal:
    """
    특정 펀드의 기준가를 크롤링
    
    Args:
        fund_cd: 모펀드 코드
        child_fund_cd: 자펀드 코드
        
    Returns:
        Decimal: 기준가
    """
    url = f"https://investments.miraeasset.com/magi/fund/view.do?fundGb=2&fundCd={fund_cd}&childFundCd={child_fund_cd}&childFundGb=2"
    
    response = requests.get(url, timeout=10)
    response.raise_for_status()
    
    return extract_current_price(response.text)


if __name__ == "__main__":
    # CLI 인자로 실행하는 경우
    if len(sys.argv) == 3:
        fund_cd = sys.argv[1]
        child_fund_cd = sys.argv[2]
        
        try:
            price = crawl_fund_price(fund_cd, child_fund_cd)
            if price is not None:
                # Java에서 파싱하기 쉽도록 숫자만 출력
                print(price)
                sys.exit(0)
            else:
                print("ERROR: 기준가를 찾을 수 없습니다.", file=sys.stderr)
                sys.exit(1)
        except Exception as e:
            print(f"ERROR: {str(e)}", file=sys.stderr)
            sys.exit(1)
    
    # 인자 없이 실행하는 경우 (테스트용)
    else:
        url = "https://investments.miraeasset.com/magi/fund/view.do?fundGb=2&fundCd=513031&childFundCd=51303C&childFundGb=2"

        print("[crawler] 페이지 요청 중...")
        response = requests.get(url, timeout=10)
        response.raise_for_status()

        price = extract_current_price(response.text)
        if price is not None:
            print(f"📈 현재 기준가: {price} 원")
        else:
            print("⚠️ 기준가를 찾을 수 없습니다.")
