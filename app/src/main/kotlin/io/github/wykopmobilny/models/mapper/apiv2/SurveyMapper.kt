package io.github.wykopmobilny.models.mapper.apiv2

import io.github.wykopmobilny.api.responses.SurveyResponse
import io.github.wykopmobilny.models.dataclass.Survey
import io.github.wykopmobilny.models.mapper.Mapper

object SurveyMapper : Mapper<SurveyResponse, Survey> {
    override fun map(value: SurveyResponse) = Survey(
        value.question ?: "",
        value.answers.orEmpty().map { AnswerMapper.map(it) },
        value.userAnswer,
    )
}
