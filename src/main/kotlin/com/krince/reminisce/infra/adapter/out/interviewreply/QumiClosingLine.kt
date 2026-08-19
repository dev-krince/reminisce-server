package com.krince.reminisce.infra.adapter.out.interviewreply

import com.krince.reminisce.domain.model.story.ChildNamePersonalizer

private const val QUMI_CLOSING_TEMPLATE =
    "ㅇㅇ이랑 이야기하니까 정말 재미있었어! " +
        "ㅇㅇ이가 좋아하는 이야기와 이야기하는 모습을 큐미가 잘 기억해둘게. " +
        "이제 ㅇㅇ이에게 잘 어울리는 이야기를 찾아볼게!"

fun qumiClosingLine(childName: String?): String = ChildNamePersonalizer.personalize(QUMI_CLOSING_TEMPLATE, childName)
