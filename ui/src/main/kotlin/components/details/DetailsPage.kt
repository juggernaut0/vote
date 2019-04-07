package components.details

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import models.ResponseDetailsView
import services.VoteService
import util.modal

class DetailsPage(private val service: VoteService) : Component() {
    private var details: List<ResponseDetailsView>? = null
    private var currentDetails: ResponseDetailsView? = null

    init {
        GlobalScope.launch {
            details = service.getResponseDetails()
            render()
        }
    }

    private fun openDetailsModal(d: ResponseDetailsView) {
        currentDetails = d
        render()
        js("jQuery")("#detailsModal").modal("show")
    }

    private fun openDeactivateModal(d: ResponseDetailsView) {
        currentDetails = d
        render()
        js("jQuery")("#deactivateModal").modal("show")
    }

    private fun deactivateResponse(d: ResponseDetailsView) {
        GlobalScope.launch {
            service.deactivateResponse(d.id)
            details = service.getResponseDetails()
            render()
        }
    }

    override fun render() {
        markup().div {
            a(Props(click = { service.goToResultsPage() })) { +"Back to results" }

            val details = details
            when {
                details == null -> p { +"Loading responses..." }
                details.isEmpty() -> p { +"Nobody has responded to this poll yet." }
                else -> table(classes("table")) {
                    thead {
                        tr {
                            th { +"Email" }
                            th {}
                            th {}
                        }
                    }
                    tbody {
                        for (d in details) {
                            tr {
                                td { +d.email }
                                td {
                                    button(Props(
                                            classes = listOf("btn", "btn-primary"),
                                            click = { openDetailsModal(d) }
                                    )) { +"View" }
                                }
                                td {
                                    button(Props(
                                            classes = listOf("btn", "btn-danger"),
                                            disabled = !d.active,
                                            click = { openDeactivateModal(d) }
                                    )) { +"Deactivate" }
                                }
                            }
                        }
                    }
                }
            }

            val d = currentDetails
            if (d != null) {
                modal("detailsModal", d.email) {
                    for (q in d.questions) {
                        h5 { +q.title }
                        ul {
                            for (r in q.responses) {
                                li { +r }
                            }
                        }
                    }
                }

                modal("deactivateModal", "Deactivate response", style = "danger", ok = { deactivateResponse(d) }) {
                    p {
                        +"Prevent this response from being counted in results? This cannot be undone."
                    }
                }
            }
        }
    }
}
